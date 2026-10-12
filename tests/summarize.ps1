. (Join-Path $PSScriptRoot "TestKit.ps1")

function Get-Bucket($n) {
    if ($n -like "AI-*") { return "AI服务" }
    if ($n -like "链路A-*") { return "链路A 收货-IQC-上架" }
    if ($n -like "链路B-*") { return "链路B 工单-生产-出库" }
    if ($n -like "链路C-*") { return "链路C NCR-MRB-CAPA-SPC" }
    if ($n -like "边界-*") { return "异常与边界" }
    if ($n -match "Token|登录|密码|白名单") { return "鉴权与安全" }
    return "system主数据"
}

$out = [System.Collections.Generic.List[string]]::new()
$g = $Global:Results | Group-Object { Get-Bucket $_.name }
foreach ($one in $g) {
    $p = @($one.Group | Where-Object { $_.pass }).Count
    $out.Add(("{0}: 总{1} PASS{2} FAIL{3}" -f $one.Name, $one.Count, $p, ($one.Count - $p)))
}
$total = $Global:Results.Count
$tp = @($Global:Results | Where-Object { $_.pass }).Count
$out.Add("合计: 总$total PASS$tp FAIL$($total-$tp)")

[IO.File]::WriteAllLines((Join-Path $PSScriptRoot "summary.txt"), $out, (New-Object Text.UTF8Encoding($true)))
