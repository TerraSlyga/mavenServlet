<#setting number_format="computer">
<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <title>Нова книга в каталозі</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 24px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);">
        <div style="text-align: center;">
            <img src="https://raw.githubusercontent.com/VladPiatachenko/MPF_Labs/lab7-freemaker/web/src/main/resources/static/img/logo.png"
                 style="width:150px; margin-bottom:20px;" alt="BookApp">
        </div>

        <h2 style="color: #2c3e50; margin-top: 0; text-align: center; border-bottom: 2px solid #3498db; padding-bottom: 10px;">
            Нова книга в каталозі!
        </h2>

        <p style="color: #555555; font-size: 15px;">
            У каталог застосунку було успішно додано нове видання:
        </p>

        <div style="background-color: #f8f9fa; border: 1px solid #e9ecef; border-left: 4px solid #3498db; border-radius: 4px; padding: 16px; margin: 20px 0;">
            <p style="margin: 8px 0; font-size: 15px; color: #333333;"><strong>Назва:</strong> ${title}</p>
            <p style="margin: 8px 0; font-size: 15px; color: #333333;"><strong>Автор:</strong> ${author}</p>
            <p style="margin: 8px 0; font-size: 15px; color: #333333;"><strong>Рік видання:</strong> ${year}</p>
            <p style="margin: 8px 0; font-size: 15px; color: #333333;"><strong>Дата додавання:</strong> ${added}</p>

            <#if year?number < 2000>
                <p style="color:#a36b00; margin: 12px 0 0 0; padding-top: 8px; border-top: 1px dashed #dcdcdc;"><b>Раритетне видання!</b></p>
            </#if>
        </div>

        <p style="color: #666666; font-size: 14px;">
            Ви можете переглянути повний список книг у веб-інтерфейсі нашого застосунку.
        </p>

        <footer style="margin-top: 30px; padding-top: 15px; border-top: 1px solid #e0e0e0; text-align: center; color: #888888; font-size: 13px;">
            BookApp &copy; 2025
        </footer>
    </div>
</body>
</html>
