<%-- 
  ToDo詳細画面
  
  選択されたToDoの詳細情報を表示する画面です。
  設計書の画面遷移図「ToDo詳細画面」に対応しています。
  
  画面遷移:
  トップ画面 → (ToDoリンク) → この画面
  この画面 → (編集ボタン) → ToDo入力フォーム画面
  この画面 → (削除ボタン) → 削除確認ダイアログ → トップ画面
  この画面 → (戻るボタン) → トップ画面
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ToDo 詳細 - ${todo.task}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <%-- ===== ヘッダー ===== --%>
        <header class="header">
            <h1>📄 ToDo 詳細</h1>
        </header>

        <%-- ===== ToDo詳細情報 ===== --%>
        <div class="detail-card">
            <div class="detail-row">
                <span class="detail-label">No.</span>
                <span class="detail-value">${todo.todoNo}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">タスク</span>
                <span class="detail-value ${todo.completed ? 'completed-text' : ''}">${todo.task}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">期日</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${todo.dueDate != null}">📅 ${todo.dueDate}</c:when>
                        <c:otherwise>期日なし</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">状態</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${todo.completed}">
                            <span class="status-badge status-done">✓ 完了</span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge status-todo">未完了</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">メモ</span>
                <span class="detail-value">${todo.memo != null ? todo.memo : 'メモなし'}</span>
            </div>
        </div>

        <%-- ===== ボタン群 ===== --%>
        <div class="detail-actions">
            <%-- 画面遷移図: (編集) → ToDo入力フォーム画面 --%>
            <a href="${pageContext.request.contextPath}/main?action=form&todoNo=${todo.todoNo}" 
               class="btn btn-primary">
                ✏️ 編集
            </a>

            <%-- 画面遷移図: (削除) → 削除確認ダイアログ → トップ画面 --%>
            <%-- アクティビティ図「ToDo削除」: 削除確認ダイアログを表示 --%>
            <form action="${pageContext.request.contextPath}/main" method="post" class="inline-form"
                  onsubmit="return confirmDelete()">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="todoNo" value="${todo.todoNo}">
                <button type="submit" class="btn btn-danger">🗑️ 削除</button>
            </form>

            <%-- 画面遷移図: (戻る) → トップ画面 --%>
            <a href="${pageContext.request.contextPath}/top" class="btn btn-secondary">← 戻る</a>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
