<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>${book.title}</title>

<h1>${book.title}</h1>
<p><i>${book.author}</i>, ${book.pubYear}</p>
</head>
<body>

<h3>Коментарі</h3>
<c:if test="${empty comments}">
    <p>Ще немає коментарів.</p>
</c:if>
<ul>
    <c:forEach var="c" items="${comments}">
        <li>
            <b>${c.author}:</b> ${c.text} <i>(${c.createdAt})</i>
            <!-- форма видалення -->
            <form method="post" action="${pageContext.request.contextPath}/comments" style="display:inline">
                <input type="hidden" name="bookId" value="${book.id}">
                <input type="hidden" name="commentId" value="${c.id}">
                <input type="hidden" name="_method" value="delete"> <!-- хак -->
                <button type="submit">Видалити</button>
            </form>
        </li>
    </c:forEach>
</ul>



<h3>Додати коментар</h3>
<form method="post" action="${pageContext.request.contextPath}/comments">
    <input type="hidden" name="bookId" value="${book.id}">
    Автор: <input type="text" name="author"><br>
    Текст: <textarea name="text"></textarea><br>
    <button type="submit">Додати</button>
</form>

<p><a href="${pageContext.request.contextPath}/books">← Повернутись до списку книг</a></p>
</body>
</html>
​