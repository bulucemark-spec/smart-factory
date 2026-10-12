. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"
$batch = "NG-PCB-" + (Get-Random -Maximum 999999)

# 1. 收货 PCB 50
$receipt = @{ warehouseId=1; supplierId=3001; supplierName="华鑫电子"
              items=@(@{ materialId=2001; materialCode="RM-PCB-01"; materialName="PCB主板"; qty=50; batchNo=$batch }) }
$rn = (Api "POST" "/api/wms/receipts/receive" $receipt $tok).data
Start-Sleep -Seconds 2

# 2. IQC：第一个定量项(长度)判 NG，其余合格
$task = @((Api "GET" "/api/qms/inspections?type=IQC&status=PENDING" $null $tok).data | Where-Object { $_.sourceNo -eq $rn })[0]
$d = (Api "GET" "/api/qms/inspections/$($task.id)/detail" $null $tok).data
$res = @(); $firstQuant = $true
foreach ($it in @($d.items)) {
    $ri = @{ itemId = $it.id }
    if ($null -ne $it.upperLimit) {
        if ($firstQuant) { $ri.measuredValue = [math]::Round([decimal]$it.upperLimit + 0.3,3); $firstQuant = $false }
        else { $ri.measuredValue = [math]::Round(([decimal]$it.upperLimit+[decimal]$it.lowerLimit)/2,3) }
    } else { $ri.judge = "OK" }
    $res += $ri
}
$r = Api "POST" "/api/qms/inspections/submit" @{ taskId=$task.id; results=$res } $tok
Add-Result "链路C-提交含NG的IQC结果" ($r.code -eq 0) "code=$($r.code)"

$t2 = @((Api "GET" "/api/qms/inspections?type=IQC" $null $tok).data | Where-Object { $_.id -eq $task.id })[0]
Add-Result "链路C-IQC任务判定UNQUALIFIED" ($t2.status -eq "UNQUALIFIED") "status=$($t2.status)"

# 3. 库存冻结 + NCR 生成
Start-Sleep -Seconds 1
$frozen = [decimal]((Api "GET" "/api/wms/inventory?materialId=2001&batchNo=$batch&status=FROZEN" $null $tok).data.records | Measure-Object qty -Sum).Sum
Add-Result "链路C-不合格批次自动冻结" ($frozen -eq 50) "冻结=$frozen"

$ncr = @((Api "GET" "/api/qms/ncrs?status=OPEN" $null $tok).data | Where-Object { $_.batchNo -eq $batch })[0]
Add-Result "链路C-自动生成NCR(OPEN)" ($null -ne $ncr) "ncr=$($ncr.ncrNo)"

# 4. MRB 评审：报废
$r = Api "POST" "/api/qms/ncrs/$($ncr.id)/mrb?disposition=SCRAP&comment=scrap-per-MRB" $null $tok
Add-Result "链路C-MRB评审处置SCRAP" ($r.code -eq 0) "code=$($r.code)"
$ncr2 = (Api "GET" "/api/qms/ncrs/$($ncr.id)" $null $tok).data
Add-Result "链路C-NCR状态REVIEWED" ($ncr2.status -eq "REVIEWED") "status=$($ncr2.status)"
$scrapped = [decimal]((Api "GET" "/api/wms/inventory?materialId=2001&batchNo=$batch&status=SCRAPPED" $null $tok).data.records | Measure-Object qty -Sum).Sum
Add-Result "链路C-WMS联动报废库存" ($scrapped -eq 50) "报废=$scrapped"
$capa = @((Api "GET" "/api/qms/capas?status=OPEN" $null $tok).data | Where-Object { $_.ncrId -eq $ncr.id })[0]
Add-Result "链路C-MRB自动生成CAPA(OPEN)" ($null -ne $capa) "capa=$($capa.capaNo)"

# 5. CAPA 执行
$capaBody = @{ id=$capa.id
    rootCause="来料尺寸超差，供应商制程漂移"
    correctiveAction="退回该批次并要求供应商整改制程"
    preventiveAction="加严IQC抽检比例并复核量具"
    ownerName="质量工程师"; dueDate="2026-11-30" }
$r = Api "POST" "/api/qms/capas/execute" $capaBody $tok
$capa2 = (Api "GET" "/api/qms/capas/$($capa.id)" $null $tok).data
Add-Result "链路C-执行纠正预防措施" ($r.code -eq 0 -and $capa2.status -eq "IN_PROGRESS") "status=$($capa2.status)"

# 6. CAPA 验证通过 → 关闭 CAPA 与 NCR
$r = Api "POST" "/api/qms/capas/$($capa.id)/verify?passed=true&verifyResult=effective" $null $tok
$capa3 = (Api "GET" "/api/qms/capas/$($capa.id)" $null $tok).data
$ncr3 = (Api "GET" "/api/qms/ncrs/$($ncr.id)" $null $tok).data
Add-Result "链路C-CAPA验证通过并关闭" ($r.code -eq 0 -and $capa3.status -eq "CLOSED") "CAPA=$($capa3.status)"
Add-Result "链路C-关联NCR联动关闭" ($ncr3.status -eq "CLOSED") "NCR=$($ncr3.status)"

# ---------- SPC 控制图与过程能力 ----------
$char = "PCB-LEN-C-" + (Get-Random -Maximum 999999)
for ($i=1; $i -le 20; $i++) {
    $v = [math]::Round(100.0 + 0.035*[math]::Sin($i), 3)
    [void](Api "POST" "/api/qms/spc/record" @{ characteristic=$char; materialCode="RM-PCB-01"; value=$v; sourceType="IQC" } $tok)
}
$ch = (Api "GET" "/api/qms/spc/chart?characteristic=$char" $null $tok).data
$chartOk = (@($ch.records).Count -eq 20 -and [double]$ch.mean -ge 99.9 -and [double]$ch.mean -le 100.1)
Add-Result "链路C-SPC控制图(20点/均值/UCL/LCL)" $chartOk "mean=$($ch.mean) ucl=$($ch.ucl) lcl=$($ch.lcl)"

$cp = (Api "GET" "/api/qms/spc/cpk?characteristic=$char&lsl=99.8&usl=100.2" $null $tok).data
Add-Result "链路C-过程能力Cpk>1.33" ([double]$cp.cpk -gt 1.33) "cp=$($cp.cp) cpk=$($cp.cpk)"

Show-Stats
