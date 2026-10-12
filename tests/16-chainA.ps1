. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"
$batch = "PCB-A-" + (Get-Random -Maximum 999999)

# 1. 收货
$receipt = @{
    warehouseId = 1
    supplierId = 3001
    supplierName = "华鑫电子"
    items = @(@{ materialId=2001; materialCode="RM-PCB-01"; materialName="PCB主板"; qty=100; batchNo=$batch })
}
$r = Api "POST" "/api/wms/receipts/receive" $receipt $tok
$receiptNo = $r.data
Add-Result "链路A-收货生成收货单" ($r.code -eq 0 -and [bool]$receiptNo) "code=$($r.code) no=$receiptNo"

Start-Sleep -Seconds 2
# 2. 待检库存
$r = Api "GET" "/api/wms/inventory?materialId=2001&batchNo=$batch&status=PENDING" $null $tok
$pendingQty = [decimal]((@($r.data.records) | Measure-Object qty -Sum).Sum)
Add-Result "链路A-收货建待检库存100" ($r.code -eq 0 -and $pendingQty -eq 100) "待检=$pendingQty"

# 3. IQC 任务
$r = Api "GET" "/api/qms/inspections?type=IQC&status=PENDING" $null $tok
$task = @($r.data | Where-Object { $_.sourceNo -eq $receiptNo })[0]
Add-Result "链路A-自动建立IQC任务" ($null -ne $task) "taskId=$($task.id)"

# 4. 录入合格结果
$r = Api "GET" "/api/qms/inspections/$($task.id)/detail" $null $tok
$results = @()
foreach ($it in @($r.data.items)) {
    $ri = @{ itemId = $it.id }
    if ($null -ne $it.upperLimit) {
        $mid = ([decimal]$it.upperLimit + [decimal]$it.lowerLimit) / 2
        $ri.measuredValue = [math]::Round($mid,3)
    } else { $ri.judge = "OK" }
    $results += $ri
}
$r = Api "POST" "/api/qms/inspections/submit" @{ taskId=$task.id; results=$results } $tok
Add-Result "链路A-IQC录入并判定合格" ($r.code -eq 0) "code=$($r.code)"

$r = Api "GET" "/api/qms/inspections?type=IQC" $null $tok
$t2 = @($r.data | Where-Object { $_.id -eq $task.id })[0]
Add-Result "链路A-IQC任务状态QUALIFIED" ($t2.status -eq "QUALIFIED") "status=$($t2.status)"

# 5. 上架任务 + 推荐库位 + 执行
$r = Api "GET" "/api/wms/putaway/tasks?status=NEW" $null $tok
$pa = @($r.data | Where-Object { $_.batchNo -eq $batch })[0]
Add-Result "链路A-合格自动建上架任务" ($null -ne $pa) "taskId=$($pa.id)"

$r = Api "GET" "/api/wms/putaway/recommend?warehouseId=1" $null $tok
$toLoc = @($r.data)[0]
Add-Result "链路A-推荐上架库位" ($null -ne $toLoc) "loc=$($toLoc.code)"

$r = Api "POST" "/api/wms/putaway/$($pa.id)/execute?toLocationId=$($toLoc.id)" $null $tok
Add-Result "链路A-执行上架" ($r.code -eq 0) "code=$($r.code)"

# 6. 校验目标合格库存 & 待检清零
$r = Api "GET" "/api/wms/inventory?materialId=2001&batchNo=$batch&status=QUALIFIED" $null $tok
$qty = [decimal]((@($r.data.records) | Where-Object { $_.locationId -eq $toLoc.id } | Measure-Object qty -Sum).Sum)
Add-Result "链路A-目标库位合格库存100" ($qty -eq 100) "合格=$qty loc=$($toLoc.code)"

$r = Api "GET" "/api/wms/inventory?materialId=2001&batchNo=$batch&status=PENDING" $null $tok
$psum = [decimal]((@($r.data.records) | Measure-Object qty -Sum).Sum)
Add-Result "链路A-待检库存已清零" ($psum -eq 0) "待检残留=$psum"

Show-Stats
