. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

$Global:Results = [System.Collections.Generic.List[object]]::new()
foreach ($o in (Load-Results)) {
    if ($o.name -ne "AI-未命中问题兜底不臆造") { [void]$Global:Results.Add($o) }
}
Save-Results

$r = Api "POST" "/api/ai/chat" @{ message="qxzwkv998877jjhh" } $tok
$c = $r.raw | ConvertFrom-Json
Add-Result "AI-未命中问题兜底不臆造" ($c.answer -match "未检索到|未找到|转人工") "sources=$(@($c.sources).Count)"
Show-Stats
