<%-- 
  トップ画面（ToDoリスト一覧）
  
  ToDoリストの一覧を表示するメイン画面です。
  設計書の画面遷移図「トップ画面」に対応しています。
  
  表示内容:
  - ToDoリスト一覧（チェックボックス付き）
  - 完了済みのToDoには取り消し線を表示（アクティビティ図に記載）
  - 新規追加ボタン
  - 各ToDoのリンク→詳細画面へ遷移
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ToDo リスト</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <%-- ===== ヘッダー ===== --%>
        <header class="header">
            <h1>📋 ToDo リスト</h1>
        </header>

        <%-- ===== 新規追加ボタン ===== --%>
        <%-- 画面遷移図: トップ画面 → (新規追加) → ToDo入力フォーム画面 --%>
        <div class="actions">
            <a href="${pageContext.request.contextPath}/main?action=form" class="btn btn-primary">
                ＋ 新規追加
            </a>
        </div>

        <%-- ===== ToDoリスト一覧 ===== --%>
        <c:choose>
            <%-- ToDoがある場合: リストを表示 --%>
            <c:when test="${not empty todoList}">
                <div class="todo-list">
                    <c:forEach var="todo" items="${todoList}">
                        <div class="todo-item ${todo.completed ? 'completed' : ''}">
                            <%-- チェックボックス: 完了/未完了の切替 --%>
                            <%-- アクティビティ図「ToDoの完了or未完了」に対応 --%>
                            <form action="${pageContext.request.contextPath}/main" method="post" class="toggle-form">
                                <input type="hidden" name="action" value="toggle">
                                <input type="hidden" name="todoNo" value="${todo.todoNo}">
                                <%-- チェック時はcompleted=trueを送信、未チェック時はfalseを送信 --%>
                                <input type="hidden" name="completed" value="${todo.completed ? 'false' : 'true'}">
                                <button type="submit" class="checkbox-btn ${todo.completed ? 'checked' : ''}" 
                                        title="${todo.completed ? '未完了にする' : '完了にする'}">
                                    ${todo.completed ? '✓' : ''}
                                </button>
                            </form>

                            <%-- ToDoの情報表示 --%>
                            <%-- 画面遷移図: (ToDoリンク) → ToDo詳細画面 --%>
                            <a href="${pageContext.request.contextPath}/main?action=detail&todoNo=${todo.todoNo}" 
                               class="todo-link">
                                <span class="todo-task">${todo.task}</span>
                                <c:if test="${todo.dueDate != null}">
                                    <span class="todo-date">📅 ${todo.dueDate}</span>
                                </c:if>
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </c:when>

            <%-- ToDoがない場合: メッセージを表示 --%>
            <%-- 設計書: 「ToDoリストがない場合はメッセージを表示」 --%>
            <c:otherwise>
                <div class="empty-message">
                    <p>📝 ToDoリストはありません。</p>
                    <p>「＋ 新規追加」ボタンからToDoを追加してください。</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
