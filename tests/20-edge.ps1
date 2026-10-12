. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

# --- 参数与格式 ---
$r = Api "POST" "/api/system/auth/login" @{ username="admin" } $null
Add-Result "边界-登录缺密码→1004" ($r.code -eq 1004) "code=$($r.code)"

$r = Api "POST" "/api/system/auth/login" "{broken json" $null
Add-Result "边界-非法JSON报文→1004" ($r.code -eq 1004) "code=$($r.code)"

$r = Api "GET" "/api/system/materials/99999999" $null $tok
Add-Result "边界-查询不存在ID返回空(建议改404)" ($r.code -eq 0 -and $null -eq $r.data) "code=$($r.code) data=$($r.data)"

# --- 库存超扣 ---
$ob = @{ warehouseId=3; type="SALES"; items=@(@{ materialId=1001; materialCode="FG-100"; materialName="智能控制器"; qty=99999 }) }
$r = Api "POST" "/api/wms/outbound" $ob $tok
Add-Result "边界-出库超扣库存→3011" ($r.code -eq 3011) "code=$($r.code)"

# --- 工单状态机：未下达即开工 ---
$woBody = @{ type="NORMAL"; productMaterialId=1001; productCode="FG-100"; productName="智能控制器"; bomId=5001; routingId=6001; qtyPlanned=5 }
$wo1 = (Api "POST" "/api/mes/work-orders" $woBody $tok).data
$r = Api "POST" "/api/mes/work-orders/$($wo1.id)/start" $null $tok
Add-Result "边界-未下达即开工→1009" ($r.code -eq 1009) "code=$($r.code)"

# --- 未拣货即交接；再无报工完工 ---
$wo2 = (Api "POST" "/api/mes/work-orders" $woBody $tok).data
[void](Api "POST" "/api/mes/work-orders/$($wo2.id)/release" $null $tok)
$r = Api "POST" "/api/mes/work-orders/$($wo2.id)/receive-materials" $null $tok
Add-Result "边界-未拣货即线边交接→1009" ($r.code -eq 1009) "code=$($r.code)"
[void](Api "POST" "/api/mes/work-orders/$($wo2.id)/start" $null $tok)
$r = Api "POST" "/api/mes/work-orders/$($wo2.id)/complete" $null $tok
Add-Result "边界-无合格报工即完工→1009" ($r.code -eq 1009) "code=$($r.code)"

# --- 重复/非法状态（复用已存在数据） ---
$iqc = @((Api "GET" "/api/qms/inspections?type=IQC" $null $tok).data | Where-Object { $_.status -ne "PENDING" })[0]
$r = Api "POST" "/api/qms/inspections/submit" @{ taskId=$iqc.id; results=@() } $tok
Add-Result "边界-检验任务重复提交→1009" ($r.code -eq 1009) "code=$($r.code) status=$($iqc.status)"

$ncr = @((Api "GET" "/api/qms/ncrs" $null $tok).data | Where-Object { $_.status -ne "OPEN" })[0]
$r = Api "POST" "/api/qms/ncrs/$($ncr.id)/mrb?disposition=SCRAP" $null $tok
Add-Result "边界-NCR重复MRB→1009" ($r.code -eq 1009) "code=$($r.code) status=$($ncr.status)"

$capa = @((Api "GET" "/api/qms/capas" $null $tok).data | Where-Object { $_.status -eq "CLOSED" })[0]
$r = Api "POST" "/api/qms/capas/$($capa.id)/verify?passed=true" $null $tok
Add-Result "边界-CAPA未执行/已关闭验证→1009" ($r.code -eq 1009) "code=$($r.code)"

$pa = @((Api "GET" "/api/wms/putaway/tasks?status=DONE" $null $tok).data)[0]
$r = Api "POST" "/api/wms/putaway/$($pa.id)/execute?toLocationId=201" $null $tok
Add-Result "边界-上架任务重复执行→1009" ($r.code -eq 1009) "code=$($r.code)"

$sh = @((Api "GET" "/api/wms/outbound" $null $tok).data | Where-Object { $_.status -eq "SHIPPED" })[0]
$r = Api "POST" "/api/wms/outbound/$($sh.id)/ship" $null $tok
Add-Result "边界-出库单重复发运→1009" ($r.code -eq 1009) "code=$($r.code)"

Show-Stats
