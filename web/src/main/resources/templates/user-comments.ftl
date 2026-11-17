<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <title>Коментарі користувача: ${username}</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background-color: #f8fafc;
            color: #1e293b;
            margin: 0;
            padding: 2rem;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: #ffffff;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        }
        .nav-link {
            display: inline-block;
            margin-bottom: 1.5rem;
            color: #2563eb;
            text-decoration: none;
            font-weight: 500;
        }
        .nav-link:hover {
            text-decoration: underline;
        }
        h1 {
            color: #0f172a;
            font-size: 1.75rem;
            margin-top: 0;
            margin-bottom: 1.5rem;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 0.75rem;
        }
        ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        li {
            padding: 1rem 0;
        }
        li p {
            margin: 0.4rem 0;
            line-height: 1.5;
        }
        hr {
            border: 0;
            height: 1px;
            background-color: #e2e8f0;
            margin: 0.75rem 0;
        }
        .empty-message {
            color: #64748b;
            font-style: italic;
        }
    </style>
</head>
<body>
<div class="container">
    <a href="/books" class="nav-link">&larr; До каталогу книг</a>

    <h1>Коментарі користувача: ${username}</h1>

    <ul>
        <#list comments as c>
            <li>
                <p><strong>${c.createdAt}</strong></p>
                <p>${c.text}</p>
                <p>Книга: <a href="/books/${c.book.id}">Book #${c.book.id}</a></p>
            </li>
            <hr>
        <#else>
            <li class="empty-message">Коментарі відсутні</li>
        </#list>
    </ul>
</div>
</body>
</html>
