. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

function PageCount($path) {
    $r = Api "GET" $path $null $tok
    return $r
}

# 分页与种子数据完整性
$cases = @(
    @{ n="组织分页(种子8条)";        p="/api/system/orgs?page=1&size=20";        min=8 },
    @{ n="角色分页(种子5条)";        p="/api/system/roles?page=1&size=20";       min=5 },
    @{ n="用户分页(种子5条)";        p="/api/system/users?page=1&size=20";       min=5 },
    @{ n="物料分页(种子6条)";        p="/api/system/materials?page=1&size=20";   min=6 },
    @{ n="供应商分页(种子2条)";      p="/api/system/suppliers?page=1&size=20";   min=2 },
    @{ n="工作中心分页(种子4条)";    p="/api/system/work-centers?page=1&size=20"; min=4 },
    @{ n="BOM分页(种子1条)";         p="/api/system/boms?page=1&size=20";        min=1 },
    @{ n="工艺路线分页(种子1条)";    p="/api/system/routings?page=1&size=20";    min=1 }
)
foreach ($c in $cases) {
    $r = PageCount $c.p
    $ok = ($r.code -eq 0 -and $r.data.total -ge $c.min)
    Add-Result $c.n $ok "code=$($r.code) total=$($r.data.total)"
}

# BOM 详情：5001 应含5个明细
$r = Api "GET" "/api/system/boms/5001/detail" $null $tok
$cnt = @($r.data.items).Count
Add-Result "BOM(5001)详情含5个明细" ($r.code -eq 0 -and $cnt -eq 5) "code=$($r.code) items=$cnt"

# 工艺路线详情：6001 应含4道工序
$r = Api "GET" "/api/system/routings/6001/detail" $null $tok
$cnt = @($r.data.operations).Count
Add-Result "工艺路线(6001)详情含4道工序" ($r.code -eq 0 -and $cnt -eq 4) "code=$($r.code) operations=$cnt"

# 物料详情
$r = Api "GET" "/api/system/materials/1001" $null $tok
Add-Result "物料(1001)详情" ($r.code -eq 0 -and $r.data.code -eq "FG-100") "code=$($r.code) material=$($r.data.code)"

# 关键字检索（用编码/英文关键字）
$r = Api "GET" "/api/system/materials?page=1&size=20&keyword=RM" $null $tok
$allRm = @($r.data.records | Where-Object { $_.code -like "RM-*" })
Add-Result "物料关键字检索(RM)" ($r.code -eq 0 -and $allRm.Count -ge 4) "命中 $($allRm.Count) 条原材料"

$r = Api "GET" "/api/system/suppliers?page=1&size=20&keyword=SUP" $null $tok
Add-Result "供应商关键字检索(SUP)" ($r.code -eq 0 -and @($r.data.records).Count -ge 2) "命中 $(@($r.data.records).Count) 条"

Show-Stats
