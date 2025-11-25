Отчёт по реализации файловой базы данных "Трекер ежедневных трат"

1. Выбранная предметная область - учёт ежедневных финансовых расходов.
База данных предназначена для отслеживания ежедневных трат пользователя с возможностью категоризации расходов, анализа финансовых привычек и ведения бюджета.

Структура данных:
- purchaseID (Long) - ключевое поле, уникальный идентификатор покупки;
- purchaseTime (LocalDateTime) - дата и время совершения покупки;
- category (String) - категория расхода (Еда, Транспорт, Развлечения и т.д.);
- amount (double) - сумма расхода;
- timezone (int) - часовой пояс для корректного учёта времени.

2. Реализованные функции
Основные операции с БД
- Создание новой базы данных
- Открытие существующей БД
- Удаление БД
- Очистка всех записей БД
- Сохранение изменений
- Создание backup-файла
- Восстановление из backup-файла

Операции с записями
- Добавление новой записи с проверкой уникальности
- Удаление записи по ключевому полю
- Удаление записей по неключевым полям
- Поиск по ключевому полю
- Поиск по неключевым полям
- Редактирование существующих записей

Дополнительные функции
- Экспорт в CSV-формат
- Графический интерфейс пользователя

3. Временная статистика и анализ сложности алгоритмов

Структуры данных
Основная структура: ConcurrentHashMap<Long, Expense>
+ Потокобезопасность и высокая производительность
- Ключ: PurchaseID (Long)
- Значение: Объект Expense

Анализ сложности операций

Добавление записи в БД
public boolean addRecord(Expense expense)
{
    if (data.containsKey(expense.getPurchaseId())) return false;
    data.put(expense.getPurchaseId(), expense);
    return true;
}

Сложность: O(1) - в среднем случае. HashMap обеспечивает константное время для операций put() и containsKey(). ~0.001-0.01 мс на операцию.

Удаление записи из БД
-По ключевому полю:
public boolean deleteByKey(Long purchaseId) { return data.remove(purchaseId) != null; }

Сложность: O(1) - в среднем случае. HashMap.remove() имеет константную сложность.

-По неключевому полю:
public List<Expense> deleteByField(String fieldName, String value)
{
    List<Expense> deleted = new ArrayList<>();
    Iterator<Map.Entry<Long, Expense>> iterator = data.entrySet().iterator();
    
    while (iterator.hasNext())
   {
        Map.Entry<Long, Expense> entry = iterator.next();
        Expense expense = entry.getValue();
        
        if (matchesField(expense, fieldName, value))
       {
            deleted.add(expense);
            iterator.remove();
        }
    }
    return deleted;
}

Сложность: O(n) - в худшем случае. Требуется полный перебор всех элементов. ~0.1-1 мс на 1000 записей.

Поиск по БД
-По ключевому полю:
public Expense searchByKey(Long purchaseId) { return data.get(purchaseId); }

Сложность: O(1) - в среднем случае. HashMap.get() имеет константную сложность.

-По неключевому полю:
public List<Expense> searchByField(String fieldName, String value)
{
    List<Expense> results = new ArrayList<>();
    for (Expense expense : data.values()) if (matchesField(expense, fieldName, value)) results.add(expense);
    return results;
}

Сложность: O(n) - в худшем случае. Линейный поиск по всем элементам. ~0.05-0.5 мс на 1000 записей.

4. Архитектурные решения

Выбор структур данных
- ConcurrentHashMap - для обеспечения потокобезопасности и быстрого доступа по ключу.
- ArrayList - для временного хранения результатов поиска и операций массового удаления.

Формат хранения данных
- Формат файла: CSV с разделителем ";". 
+ Простота реализации, читаемость, совместимость.
- Отсутствие типизации при загрузке.

Обработка ошибок
- Единая система обработки исключений через IOException.
- Валидация данных на уровне GUI.
- Проверка уникальности ключевых полей.
