/**
 * ToDo リスト アプリケーション - JavaScript
 *
 * 画面上のユーザー操作を補助するJavaScriptファイルです。
 * 手順書ステップ6「HTML/CSS、JavaScriptで画面を作成する」に対応しています。
 *
 * 主な機能:
 * - 削除確認ダイアログの表示（設計書: アクティビティ図「ToDo削除」）
 * - フォームのバリデーション（入力チェック）
 * - キャンセル時の編集破棄確認（設計書: アクティビティ図「ToDo新規追加」「ToDo編集」）
 */

/**
 * 削除確認ダイアログ
 *
 * 設計書のアクティビティ図「ToDo削除」では、
 * 削除ボタン押下時に確認ダイアログを表示し、
 * OKなら削除、キャンセルなら画面に戻る、と定義されています。
 *
 * @returns {boolean} true=削除実行, false=キャンセル
 */
function confirmDelete() {
    return confirm("本当にこのToDoを削除しますか？");
}

/**
 * フォームのバリデーション（入力チェック）
 *
 * タスク名が空でないかをチェックします。
 * HTML5のrequired属性でも基本チェックされますが、
 * JavaScript側でも二重チェックして安全性を高めています。
 *
 * @param {HTMLFormElement} form チェック対象のフォーム要素
 * @returns {boolean} true=送信OK, false=送信中止
 */
function validateForm(form) {
    var task = form.querySelector("#task");
    if (task && task.value.trim() === "") {
        alert("タスク名を入力してください。");
        task.focus();
        return false;
    }
    return true;
}

/**
 * フォームのキャンセル処理
 *
 * 設計書のアクティビティ図「ToDo新規追加」「ToDo編集」では、
 * キャンセルボタン押下時に「編集内容破棄の確認ダイアログ」を表示し、
 * OK → トップ画面に遷移、キャンセル → フォームに戻る、と定義されています。
 */
function cancelForm() {
    if (confirm("入力内容を破棄してもよろしいですか？")) {
        // OKが押された場合、ブラウザの履歴を1つ戻る（トップ画面に戻る）
        window.history.back();
    }
    // キャンセルが押された場合は何もしない（フォームに留まる）
}
