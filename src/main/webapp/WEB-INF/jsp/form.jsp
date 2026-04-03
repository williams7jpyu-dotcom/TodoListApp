<%-- 
  ToDo入力フォーム画面（新規追加/編集共用）
  
  ToDoの新規追加と編集を行う入力フォーム画面です。
  設計書の画面遷移図「ToDo入力フォーム画面」に対応しています。
  
  新規追加/編集の判定:
  todo属性がnull → 新規追加モード
  todo属性がある → 編集モード（既存値がフォームに表示される）
  
  画面遷移:
  トップ画面 → (新規追加) → この画面
  ToDo詳細画面 → (編集) → この画面
  この画面 → (保存) → トップ画面
  この画面 → (キャンセル) → トップ画面 or 戻る
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ToDo ${todo != null ? '編集' : '新規追加'}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <%-- ===== ヘッダー ===== --%>
        <header class="header">
            <h1>${todo != null ? '✏️ ToDo 編集' : '➕ ToDo 新規追加'}</h1>
        </header>

        <%-- ===== 入力フォーム ===== --%>
        <%-- 新規追加: action=add, 編集: action=update --%>
        <form action="${pageContext.request.contextPath}/main" method="post" class="todo-form"
              onsubmit="return validateForm(this)">
            <%-- actionパラメータで新規追加/更新を区別 --%>
            <input type="hidden" name="action" value="${todo != null ? 'update' : 'add'}">

            <%-- 編集時はtodoNoをhiddenで送信 --%>
            <c:if test="${todo != null}">
                <input type="hidden" name="todoNo" value="${todo.todoNo}">
                <%-- 編集時は既存の完了状態を引き継ぐ --%>
                <input type="hidden" name="completed" value="${todo.completed}">
            </c:if>

            <%-- タスク名入力 --%>
            <div class="form-group">
                <label for="task" class="form-label">タスク名 <span class="required">*必須</span></label>
                <input type="text" id="task" name="task" class="form-input" 
                       value="${todo != null ? todo.task : ''}" 
                       placeholder="例: 買い物に行く" required>
            </div>

            <%-- 期日入力 --%>
            <div class="form-group">
                <label for="dueDate" class="form-label">期日</label>
                <input type="date" id="dueDate" name="dueDate" class="form-input" 
                       value="${todo != null && todo.dueDate != null ? todo.dueDate : ''}">
            </div>

            <%-- メモ入力 --%>
            <div class="form-group">
                <label for="memo" class="form-label">メモ</label>
                <textarea id="memo" name="memo" class="form-input form-textarea" 
                          rows="3" placeholder="補足情報を入力（任意）">${todo != null && todo.memo != null ? todo.memo : ''}</textarea>
            </div>

            <%-- ===== ボタン群 ===== --%>
            <div class="form-actions">
                <%-- 保存ボタン --%>
                <button type="submit" class="btn btn-primary">💾 保存</button>

                <%-- キャンセルボタン --%>
                <%-- アクティビティ図: キャンセル時は編集破棄の確認ダイアログを表示 --%>
                <button type="button" class="btn btn-secondary" onclick="cancelForm()">キャンセル</button>
            </div>
        </form>
    </div>

    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
