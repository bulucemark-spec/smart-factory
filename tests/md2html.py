# -*- coding: utf-8 -*-
"""零依赖 Markdown -> HTML 转换器（覆盖本项目文档用到的语法）"""
import re, html, io, sys, os

def esc(s):
    return html.escape(s, quote=False)

def inline(s):
    s = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", s)
    s = re.sub(r"\*(.+?)\*", r"<em>\1</em>", s)
    s = re.sub(r"`([^`]+?)`", lambda m: "<code>" + esc(m.group(1)) + "</code>", s)
    s = re.sub(r"!\[([^\]]*)\]\(([^)]+)\)", r'<img alt="\1" src="\2">', s)
    s = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<a href="\2">\1</a>', s)
    return s

def convert(md):
    code_blocks = []
    def save_code(m):
        code_blocks.append(m.group(2))
        return "@@CODE%d@@" % (len(code_blocks) - 1)
    text = re.sub(r"```(\w*)\n(.*?)```", save_code, md, flags=re.S)

    lines = text.split("\n")
    out = []
    i, n = 0, len(lines)
    while i < n:
        line = lines[i]
        stripped = line.strip()

        if stripped.startswith("@@"):
            out.append(line)
            i += 1
            continue

        # 标题
        m = re.match(r"^(#{1,6})\s+(.*)$", line)
        if m:
            lv = len(m.group(1))
            out.append("<h%d>%s</h%d>" % (lv, inline(m.group(2)), lv))
            i += 1
            continue

        # 表格
        if stripped.startswith("|") or ("|" in stripped and i + 1 < n and re.match(r"^\s*\|?[\s:|-]+\|?\s*$", lines[i+1].strip())):
            rows = []
            while i < n and lines[i].strip().startswith("|"):
                cell = lines[i].strip().strip("|")
                rows.append([c.strip() for c in cell.split("|")])
                i += 1
            if len(rows) >= 2:
                # 跳过分隔行
                body = [r for r in rows if not re.match(r"^[\s:|-]+$", "".join(r))]
                thead = body[0]
                out.append("<table><thead><tr>" + "".join("<th>%s</th>" % inline(c) for c in thead) + "</tr></thead><tbody>")
                for r in body[1:]:
                    out.append("<tr>" + "".join("<td>%s</td>" % inline(c) for c in r) + "</tr>")
                out.append("</tbody></table>")
                continue
            else:
                # 不是合法表格，按普通文本
                pass

        # 分隔线
        if re.match(r"^\s*(-{3,}|\*{3,}|_{3,})\s*$", line):
            out.append("<hr>")
            i += 1
            continue

        # 引用
        if stripped.startswith(">"):
            buf = []
            while i < n and lines[i].strip().startswith(">"):
                buf.append(re.sub(r"^>\s?", "", lines[i]))
                i += 1
            out.append("<blockquote>" + inline("<br>".join(buf)) + "</blockquote>")
            continue

        # 无序列表
        if re.match(r"^\s*[-*+]\s+", line):
            items = []
            while i < n and re.match(r"^\s*[-*+]\s+", lines[i]):
                items.append(inline(re.sub(r"^\s*[-*+]\s+", "", lines[i])))
                i += 1
            out.append("<ul>" + "".join("<li>%s</li>" % it for it in items) + "</ul>")
            continue

        # 有序列表
        if re.match(r"^\s*\d+\.\s+", line):
            items = []
            while i < n and re.match(r"^\s*\d+\.\s+", lines[i]):
                items.append(inline(re.sub(r"^\s*\d+\.\s+", "", lines[i])))
                i += 1
            out.append("<ol>" + "".join("<li>%s</li>" % it for it in items) + "</ol>")
            continue

        # 空行
        if stripped == "":
            out.append("")
            i += 1
            continue

        # 普通段落（合并连续行）
        para = [line]
        i += 1
        while i < n and lines[i].strip() and not re.match(r"^(#{1,6}\s|```|>|\s*[-*+]\s+|\s*\d+\.\s+|---+|\|)", lines[i].strip()):
            para.append(lines[i].strip())
            i += 1
        out.append("<p>%s</p>" % inline(" ".join(para)))

    result = "\n".join(out)
    # 还原代码块
    def restore(m):
        idx = int(m.group(1))
        code = code_blocks[idx].rstrip("\n")
        return "<pre><code>" + esc(code) + "</code></pre>"
    result = re.sub(r"@@CODE(\d+)@@", restore, result)
    return result

def build_html(title, body):
    css = """
    body{font-family:'Microsoft YaHei',Segoe UI,sans-serif;line-height:1.75;max-width:960px;margin:24px auto;padding:0 24px;color:#24292f;background:#fff}
    h1{border-bottom:2px solid #0969da;padding-bottom:8px;font-size:1.9em}
    h2{border-bottom:1px solid #d8dee4;padding-bottom:6px;margin-top:28px;font-size:1.5em}
    h3{margin-top:22px;font-size:1.25em}
    table{border-collapse:collapse;width:100%;margin:14px 0;font-size:.95em}
    th,td{border:1px solid #d0d7de;padding:7px 12px;text-align:left}
    th{background:#f6f8fa}
    pre{background:#f6f8fa;border:1px solid #d0d7de;border-radius:6px;padding:14px;overflow-x:auto;font-size:.9em}
    code{background:#eff1f3;border-radius:4px;padding:2px 5px;font-family:Consolas,monospace}
    pre code{background:none;padding:0}
    blockquote{border-left:4px solid #0969da;margin:12px 0;padding:6px 14px;color:#57606a;background:#f6f8fa}
    ul,ol{padding-left:26px}
    a{color:#0969da}
    img{max-width:100%}
    """
    return ("<!DOCTYPE html><html lang='zh-CN'><head><meta charset='utf-8'>"
            "<title>%s</title><style>%s</style></head><body>%s</body></html>"
            % (esc(title), css, body))

def main():
    src, dst, title = sys.argv[1], sys.argv[2], sys.argv[3]
    with io.open(src, "r", encoding="utf-8") as f:
        md = f.read()
    body = convert(md)
    with io.open(dst, "w", encoding="utf-8") as f:
        f.write(build_html(title, body))
    print("OK -> " + dst)

if __name__ == "__main__":
    main()
