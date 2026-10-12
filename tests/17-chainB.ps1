. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

# ---------- 前置：按 BOM 备齐原材料（收货→IQC合格→上架） ----------
$mats = @(
    @{id=2001;code="RM-PCB-01";name="PCB主板";qty=50;sup=3001;supName="华鑫电子"},
    @{id=2002;code="RM-CASE-01";name="塑料外壳";qty=50;sup=3002;supName="恒达塑胶"},
    @{id=2003;code="RM-CHIP-01";name="主控芯片";qty=50;sup=3001;supName="华鑫电子"},
    @{id=2004;code="RM-SCR-01";name="十字螺丝";qty=200;sup=3001;supName="华鑫电子"},
    @{id=2005;code="PK-BOX-01";name="包装盒";qty=50;sup=3001;supName="华鑫电子"}
)
function Replenish($m) {
    $b = "RP-" + $m.code + "-" + (Get-Random -Maximum 99999)
    $receipt = @{ warehouseId=1; supplierId=$m.sup; supplierName=$m.supName
                  items=@(@{ materialId=$m.id; materialCode=$m.code; materialName=$m.name; qty=$m.qty; batchNo=$b }) }
    $rn = (Api "POST" "/api/wms/receipts/receive" $receipt $tok).data
    Start-Sleep -Seconds 2
    $task = @((Api "GET" "/api/qms/inspections?type=IQC&status=PENDING" $null $tok).data | Where-Object { $_.sourceNo -eq $rn })[0]
    $d = (Api "GET" "/api/qms/inspections/$($task.id)/detail" $null $tok).data
    $res = @()
    foreach ($it in @($d.items)) {
        $ri = @{ itemId = $it.id }
        if ($null -ne $it.upperLimit) { $ri.measuredValue = [math]::Round(([decimal]$it.upperLimit+[decimal]$it.lowerLimit)/2,3) }
        else { $ri.judge = "OK" }
        $res += $ri
    }
    [void](Api "POST" "/api/qms/inspections/submit" @{ taskId=$task.id; results=$res } $tok)
    Start-Sleep -Seconds 1
    $pa = @((Api "GET" "/api/wms/putaway/tasks?status=NEW" $null $tok).data | Where-Object { $_.batchNo -eq $b })[0]
    $loc = @((Api "GET" "/api/wms/putaway/recommend?warehouseId=1" $null $tok).data)[0]
    [void](Api "POST" "/api/wms/putaway/$($pa.id)/execute?toLocationId=$($loc.id)" $null $tok)
}
foreach ($m in $mats) {
    Replenish $m
    $need = if ($m.id -eq 2004) { 40 } else { 10 }
    $have = [decimal]((Api "GET" "/api/wms/inventory?materialId=$($m.id)&status=QUALIFIED" $null $tok).data.records | Measure-Object qty -Sum).Sum
    Add-Result "链路B-备料 $($m.code) 合格库存充足" ($have -ge $need) "合格=$have 需求=$need"
}

# ---------- 1. 创建工单（10台） ----------
$woBody = @{ type="NORMAL"; productMaterialId=1001; productCode="FG-100"; productName="智能控制器"
             bomId=5001; routingId=6001; workCenterId=4002; qtyPlanned=10 }
$r = Api "POST" "/api/mes/work-orders" $woBody $tok
$wo = $r.data
Add-Result "链路B-创建工单" ($r.code -eq 0 -and $wo.status -eq "CREATED") "wo=$($wo.orderNo) status=$($wo.status)"

# ---------- 2. 下达并按 BOM 展开领料 ----------
$wave = @{ warehouseId=1; items=@(
    @{materialId=2001;materialCode="RM-PCB-01";materialName="PCB主板";qty=10},
    @{materialId=2002;materialCode="RM-CASE-01";materialName="塑料外壳";qty=10},
    @{materialId=2003;materialCode="RM-CHIP-01";materialName="主控芯片";qty=10},
    @{materialId=2004;materialCode="RM-SCR-01";materialName="十字螺丝";qty=40},
    @{materialId=2005;materialCode="PK-BOX-01";materialName="包装盒";qty=10}
)}
$r = Api "POST" "/api/mes/work-orders/$($wo.id)/release" $wave $tok
Start-Sleep -Seconds 2
$wv = @((Api "GET" "/api/wms/pick/waves" $null $tok).data | Where-Object { $_.sourceNo -eq $wo.orderNo })[0]
Add-Result "链路B-下达工单并生成领料波次" ($r.code -eq 0 -and $null -ne $wv) "wave=$($wv.waveNo)"

# ---------- 3. 执行拣货（条件扣减） ----------
$r = Api "POST" "/api/wms/pick/waves/$($wv.id)/execute" $null $tok
$wv2 = @((Api "GET" "/api/wms/pick/waves" $null $tok).data | Where-Object { $_.id -eq $wv.id })[0]
Add-Result "链路B-执行拣货扣减合格库存" ($r.code -eq 0 -and $wv2.status -eq "PICKED") "wave状态=$($wv2.status)"

# ---------- 4. 线边交接 → 投料/谱系 ----------
$r = Api "POST" "/api/mes/work-orders/$($wo.id)/receive-materials" $null $tok
$wo2 = (Api "GET" "/api/mes/work-orders/$($wo.id)" $null $tok).data
Add-Result "链路B-线边交接回写投料与谱系" ($r.code -eq 0 -and $wo2.status -eq "PICKED") "status=$($wo2.status)"

# ---------- 5. 开工 ----------
$r = Api "POST" "/api/mes/work-orders/$($wo.id)/start" $null $tok
$wo2 = (Api "GET" "/api/mes/work-orders/$($wo.id)" $null $tok).data
Add-Result "链路B-开工" ($r.code -eq 0 -and $wo2.status -eq "STARTED") "status=$($wo2.status)"

# ---------- 6. 报工（10合格） ----------
$rep = @{ workOrderId=$wo.id; workstationId=2; operationSeq=20; operationName="整机组装"; goodQty=10; defectQty=0; scrapQty=0 }
$r = Api "POST" "/api/mes/work-orders/report" $rep $tok
$wo2 = (Api "GET" "/api/mes/work-orders/$($wo.id)" $null $tok).data
Add-Result "链路B-报工累计合格10" ($r.code -eq 0 -and [decimal]$wo2.qtyCompleted -eq 10) "完成=$($wo2.qtyCompleted)"

# ---------- 7. 完工：成品批次 + 入库 + OQC ----------
$r = Api "POST" "/api/mes/work-orders/$($wo.id)/complete" $null $tok
$wo2 = (Api "GET" "/api/mes/work-orders/$($wo.id)" $null $tok).data
$fgb = $wo2.finishedBatch
Add-Result "链路B-完工生成成品批次并入库" ($r.code -eq 0 -and $wo2.status -eq "STOCKED" -and [bool]$fgb) "成品批次=$fgb"

# ---------- 8. OQC 合格放行 ----------
Start-Sleep -Seconds 2
$oqc = @((Api "GET" "/api/qms/inspections?type=OQC&status=PENDING" $null $tok).data | Where-Object { $_.sourceNo -eq $wo.orderNo })[0]
$d = (Api "GET" "/api/qms/inspections/$($oqc.id)/detail" $null $tok).data
$res = @(); foreach ($it in @($d.items)) { $res += @{ itemId=$it.id; judge="OK" } }
$r = Api "POST" "/api/qms/inspections/submit" @{ taskId=$oqc.id; results=$res } $tok
$oqc2 = @((Api "GET" "/api/qms/inspections?type=OQC" $null $tok).data | Where-Object { $_.id -eq $oqc.id })[0]
Add-Result "链路B-OQC合格放行成品" ($r.code -eq 0 -and $oqc2.status -eq "QUALIFIED") "OQC=$($oqc2.status)"

$have = [decimal]((Api "GET" "/api/wms/inventory?warehouseId=3&materialId=1001&batchNo=$fgb&status=QUALIFIED" $null $tok).data.records | Measure-Object qty -Sum).Sum
Add-Result "链路B-成品仓合格库存10" ($have -eq 10) "成品合格=$have"

# ---------- 9. 出库并发运 ----------
$obBody = @{ warehouseId=3; type="SALES"; customer="华东客户"
             items=@(@{ materialId=1001; materialCode="FG-100"; materialName="智能控制器"; qty=10 }) }
$r = Api "POST" "/api/wms/outbound" $obBody $tok
$obNo = $r.data
Add-Result "链路B-创建出库单" ($r.code -eq 0 -and [bool]$obNo) "ob=$obNo"
$obId = @((Api "GET" "/api/wms/outbound" $null $tok).data | Where-Object { $_.outboundNo -eq $obNo })[0].id
$r = Api "POST" "/api/wms/outbound/$obId/ship" $null $tok
$ob2 = @((Api "GET" "/api/wms/outbound" $null $tok).data | Where-Object { $_.id -eq $obId })[0]
Add-Result "链路B-确认发运" ($r.code -eq 0 -and $ob2.status -eq "SHIPPED") "状态=$($ob2.status)"

$left = [decimal]((Api "GET" "/api/wms/inventory?warehouseId=3&materialId=1001&batchNo=$fgb&status=QUALIFIED" $null $tok).data.records | Measure-Object qty -Sum).Sum
Add-Result "链路B-发运后成品库存清零" ($left -eq 0) "残留=$left"

# ---------- 10. 追溯链 ----------
$back = @((Api "GET" "/api/mes/genealogy/backward/$fgb" $null $tok).data)
Add-Result "链路B-反向追溯(成品→原料批次)" ($back.Count -ge 5) "子件记录=$($back.Count)"
$fwd = Api "GET" "/api/mes/genealogy/forward/$fgb" $null $tok
Add-Result "链路B-正向追溯接口可用" ($fwd.code -eq 0) "code=$($fwd.code)"

Show-Stats
