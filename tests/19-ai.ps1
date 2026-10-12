. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

# 健康检查（原生 JSON）
$h = ((Api "GET" "/api/ai/health" $null $null).raw | ConvertFrom-Json)
Add-Result "AI-健康检查UP" ($h.status -eq "UP") "status=$($h.status) llm=$($h.llm_enabled)"
Add-Result "AI-知识库已加载文档块" ($h.documents -gt 0) "documents=$($h.documents)"

# 未授权访问 chat 被拦截
$r = Api "POST" "/api/ai/chat" @{ message="hi" } $null
Add-Result "AI-未授权chat被拦截" ($r.code -eq 1003) "code=$($r.code) http=$($r.http)"

# 智能问答（命中知识库）
$r = Api "POST" "/api/ai/chat" @{ message="IQC来料检验流程是什么？"; history=$null } $tok
$c = $r.raw | ConvertFrom-Json
Add-Result "AI-智能问答返回答案" ([bool]$c.answer) "answer长度=$($c.answer.Length)"
Add-Result "AI-问答附带检索来源" (@($c.sources).Count -ge 1) "sources=$(@($c.sources).Count)"

# 未命中问题 → 兜底转人工
$r = Api "POST" "/api/ai/chat" @{ message="qxzwkv998877jjhh" } $tok
$c2 = $r.raw | ConvertFrom-Json
Add-Result "AI-未命中问题兜底不臆造" ($c2.answer -match "未检索到|未找到|转人工") "sources=$(@($c2.sources).Count)"

# 根因分析
$body = @{ defect="PCB长度超差"; product="智能控制器"; process="SMT贴片"; batch_no="NG-001"; extra="不良率5%" }
$r = Api "POST" "/api/ai/root-cause" $body $tok
$rc = $r.raw | ConvertFrom-Json
Add-Result "AI-根因分析返回" ([bool]$rc.analysis) "analysis长度=$($rc.analysis.Length)"
Add-Result "AI-根因给出人机料法环原因" (@($rc.possible_causes).Count -ge 3) "causes=$(@($rc.possible_causes).Count)"

# 模式确认：未配置 key 自动 Mock
Add-Result "AI-无APIKey自动Mock模式" ($c.llm_enabled -eq $false) "llm_enabled=$($c.llm_enabled)"

Show-Stats
