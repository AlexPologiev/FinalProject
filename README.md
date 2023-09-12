**Search Engine**(Поисковый движок)

Поисковый движок предназначен для поиска текстовых запросов пользователя на заранее проиндексированых сайтах.

**Используемые технологии:**

Java 17, Spring Boot 2.7(WEB, MVC, Data JPA), ForkJoinPool

**Принципы работы поискового движка:**
1. В конфигурационном файле перед запуском приложения задаются
адреса сайтов, по которым движок должен осуществлять поиск.
2. Поисковый движок должен самостоятельно обходить все страницы
заданных сайтов и индексировать их (создавать так называемый индекс)
так, чтобы потом находить наиболее релевантные страницы по любому
поисковому запросу.
3. Пользователь присылает запрос через API движка. Запрос — это набор
слов, по которым нужно найти страницы сайта.
4. Запрос определённым образом трансформируется в список слов,
переведённых в базовую форму. Например, для существительных —
именительный падеж, единственное число.
5. В индексе ищутся страницы, на которых встречаются все эти слова.
6. Результаты поиска ранжируются, сортируются и отдаются пользователю.

**Описание веб-интерфейса**

Веб-интерфейс (frontend-составляющая) проекта представляет собой
одну веб-страницу с тремя вкладками:

● **Dashboard.** Эта вкладка открывается по умолчанию. На ней
отображается общая статистика по всем сайтам, а также детальная
статистика и статус по каждому из сайтов (статистика, получаемая по
запросу /api/statistics).

● **Management.** На этой вкладке находятся инструменты управления
поисковым движком — запуск и остановка полной индексации
(переиндексации), а также возможность добавить (обновить) отдельную
страницу по ссылке:

● **Search.** Эта страница предназначена для тестирования поискового
движка. На ней находится поле поиска, выпадающий список с выбором
сайта для поиска, а при нажатии на кнопку «Найти» выводятся
результаты поиска (по API-запросу /api/search):
Вся информация на вкладки подгружается путём запросов к API вашего
приложения. При нажатии кнопок также отправляются запросы.
