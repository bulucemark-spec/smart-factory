. (Join-Path $PSScriptRoot "TestKit.ps1")

# 1. 无token访问受保护接口
$r = Api "GET" "/api/system/materials?page=1&size=1" $null $null
Add-Result "无Token访问物料接口被拦截" ($r.code -eq 1003 -or $r.http -eq "401") "code=$($r.code) http=$($r.http)"

$r = Api "GET" "/api/wms/inventory?page=1&size=1" $null $null
Add-Result "无Token访问WMS库存被拦截" ($r.code -eq 1003 -or $r.http -eq "401") "code=$($r.code) http=$($r.http)"

# 2. 伪造/非法 token
$r = Api "GET" "/api/system/materials?page=1&size=1" $null "abc.def.ghi"
Add-Result "伪造Token被拒绝" ($r.code -eq 1003 -or $r.http -eq "401") "code=$($r.code) http=$($r.http)"

$r = Api "GET" "/api/system/materials?page=1&size=1" $null "not-a-valid-jwt"
Add-Result "非法格式Token被拒绝" ($r.code -eq 1003 -or $r.http -eq "401") "code=$($r.code) http=$($r.http)"

# 3. 白名单：AI 健康检查无需token
$r = Api "GET" "/api/ai/health" $null $null
$h = $r.raw | ConvertFrom-Json
Add-Result "白名单AI健康检查可匿名" ($r.http -eq "200" -and $h.status -eq "UP") "http=$($r.http) status=$($h.status)"

# 4. 登录：错误密码
$r = Api "POST" "/api/system/auth/login" @{ username="admin"; password="wrongpass" } $null
Add-Result "错误密码登录被拒" ($r.code -ne 0) "code=$($r.code) msg=$($r.message)"

# 5. 登录：不存在用户
$r = Api "POST" "/api/system/auth/login" @{ username="nobody_xyz"; password="x" } $null
Add-Result "不存在用户登录被拒" ($r.code -ne 0) "code=$($r.code)"

# 6. 正确登录 admin
$tok = Login "admin" "admin123"
Add-Result "admin正确登录并签发Token" ([bool]$tok) "token长度=$($tok.Length)"

# 7. /auth/me 解析当前用户
$r = Api "GET" "/api/system/auth/me" $null $tok
Add-Result "Token解析当前用户(/me)" ($r.code -eq 0 -and $r.data.username -eq "admin") "code=$($r.code) user=$($r.data.username)"

# 8. 各角色账号登录
foreach ($u in @("planner","warehouse","inspector","operator")) {
    $t = Login $u ($u+"123")
    Add-Result "角色账号 $u 登录" ([bool]$t) "ok=$([bool]$t)"
}

Show-Stats
