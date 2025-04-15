📝《MyNote 筆記 App》功能總整理（截至 2025/04/15）
#ChatGPT 排版

🌟 主要功能功能

📄 筆記卡片顯示
筆記以網格（Grid）形式呈現，每張卡片包含：
・標題（加粗）
・建立時間
・內容摘要（前面 2~3 行）
・圓角設計與柔和陰影

➕ 新增筆記
右下角浮動按鈕（FAB）可新增筆記

🖊️ 筆記編輯畫面
開啟筆記後顯示：
・標題位於第一行、字體較大粗體
・下方為內文區塊
・兩者樣式有明確區隔

🔍 搜尋功能
搜尋欄可輸入關鍵字即時篩選

✨ 搜尋關鍵字高亮
在標題與摘要中，高亮顯示符合的關鍵字（支援忽略大小寫、螢光筆背景效果）

💾 自動儲存功能
編輯筆記內容時預設會自動儲存變更，避免資料遺失

⚙️ 自動儲存設定
在設定頁面中提供開關，可選擇啟用或停用自動儲存功能

🎨 UI / UX 設計

・整體簡約風格（類似小米筆記）
・白色背景、圓角卡片設計
・使用 Material 3 元件

頁面上方設有：
・搜尋欄（OutlinedTextField）
・篩選下拉選單（DropdownMenu）

所有字體清晰可讀、佈局整齊

🌗 深色模式支援

・根據系統設定自動切換 Light / Dark Mode
・使用 MaterialTheme + ColorScheme 設定不同配色
・Compose 顏色自動對應背景、字體與卡片樣式

⚙️ 技術架構與架設工具

・開發工具：Android Studio（Empty Activity 模板）+ Cursor（Claude 3.7 Sonnet / Gemini 2.5 Pro Exp 模型）
・架構：Jetpack Compose + Material 3
・使用 LazyVerticalGrid 呈現筆記列表
・使用 AnnotatedString + SpanStyle 實作關鍵字高亮

孩子們等我學完富文本我再做更多更新，先這樣了。
