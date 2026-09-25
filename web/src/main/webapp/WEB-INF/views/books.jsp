<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Каталог книг</title>
</head>
<body>
<h1>Каталог книг</h1>
<ul>
    <c:forEach var="book" items="${books}">
        <li>
            <a href="${pageContext.request.contextPath}/comments?bookId=${book.id}">
                    ${book.title} — ${book.author} (${book.pubYear})
            </a>
        </li>
    </c:forEach>
</ul>
</body>
</html>
