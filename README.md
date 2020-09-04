# Решения домашних заданий курса "Технологии Java"
## Санкт-Петербург, Университет ИТМО, КТ, 2019-2020 год


## Домашнее задание 11. Физические лица

Добавьте к RMI-банковскому приложению возможность работы с физическими лицами.
* У физического лица (Person) можно запросить имя, фамилию и номер паспорта.
* Локальные физические лица (LocalPerson) должны передаваться при помощи механизма сериализации.
* Удалённые физические лица (RemotePerson) должны передаваться при помощи удалённых объектов.
* Должна быть возможность поиска физического лица по номеру паспорта, с выбором типа возвращаемого лица.
* Должна быть возможность создания записи о физическом лице по его данным.
* У физического лица может быть несколько счетов, к которым должен предоставляться доступ.
* Счету физического лица с идентификатором subId должен соответствовать банковский счет с id вида passport:subId.
* Изменения, производимые со счетом в банке (создание и изменение баланса), должны быть видны всем соответствующим RemotePerson, и только тем LocalPerson, которые были созданы после этого изменения.
* Изменения в счетах, производимые через RemotePerson, должны сразу применяться глобально, а производимые через LocalPerson – только локально для этого конкретного LocalPerson.

Реализуйте приложение, демонстрирующее работу с физическим лицами.
* Аргументы командной строки: имя, фамилия, номер паспорта физического лица, номер счета, изменение суммы счета.
* Если информация об указанном физическом лице отсутствует, то оно должно быть добавлено. В противном случае – должны быть проверены его данные.
* Если у физического лица отсутствует счет с указанным номером, то он создается с нулевым балансом.
* После обновления суммы счета новый баланс должен выводиться на консоль.

Напишите тесты, проверяющее вышеуказанное поведение как банка, так и приложения.
* Для реализации тестов рекомендуется использовать JUnit (Tutorial). Множество примеров использования можно найти в тестах.
* Если вы знакомы с другим тестовым фреймворком (например, TestNG), то можете использовать его.
* Использовать самописные фреймворки и тесты запускаемые через main нельзя.
* Весь код должен находиться в пакете ru.ifmo.rain.фамилия.bank и его подпакетах

Сложный вариант
* Тесты не должны рассчитывать на наличие запущенного RMI Registry.
* Создайте класс BankTests, запускающий тесты.
* Создайте скрипт, запускающий BankTests и возвращающий код (статус) 0 в случае успеха и 1 в случае неудачи.
* Создайте скрипт, запускающий тесты с использованием стандартного подхода для вашего тестового фреймворка. Код возврата должен быть как в предыдущем пункте.


## Домашнее задание 10. HelloUDP

Реализуйте клиент и сервер, взаимодействующие по UDP.
Класс `HelloUDPClient` должен отправлять запросы на сервер, принимать результаты и выводить их на консоль.

Аргументы командной строки:
* имя или ip-адрес компьютера, на котором запущен сервер;
* номер порта, на который отсылать запросы;
* префикс запросов (строка);
* число параллельных потоков запросов;
* число запросов в каждом потоке.
Запросы должны одновременно отсылаться в указанном числе потоков. Каждый поток должен ожидать обработки своего запроса и выводить сам запрос и результат его обработки на консоль. Если запрос не был обработан, требуется послать его заново.

Запросы должны формироваться по схеме `<префикс запросов><номер потока>_<номер запроса в потоке>`.
Класс `HelloUDPServer` должен принимать задания, отсылаемые классом `HelloUDPClient` и отвечать на них.

Аргументы командной строки:
* номер порта, по которому будут приниматься запросы;
* число рабочих потоков, которые будут обрабатывать запросы.

Ответом на запрос должно быть `Hello, <текст запроса>`.
Если сервер не успевает обрабатывать запросы, прием запросов может быть временно приостановлен.


## Домашнее задание 9. Web Crawler

Напишите потокобезопасный класс `WebCrawler`, который будет рекурсивно обходить сайты.

Класс `WebCrawler` должен иметь конструктор
```java
public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost)
```             
* downloader позволяет скачивать страницы и извлекать из них ссылки;
* downloaders — максимальное число одновременно загружаемых страниц;
* extractors — максимальное число страниц, из которых извлекаются ссылки;
* perHost — максимальное число страниц, одновременно загружаемых c одного хоста. Для опредения хоста следует использовать метод `getHost` класса `URLUtils` из пакета `info.kgeorgiy`.
Класс `WebCrawler` должен реализовывать интерфейс `Crawler`
```java
public interface Crawler extends AutoCloseable {
   Result download(String url, int depth);

   void close();
}
```             
Метод `download` должен рекурсивно обходить страницы, начиная с указанного URL на указанную глубину и возвращать список загруженных страниц и файлов. Например, если глубина равна 1, то должна быть загружена только указанная страница. Если глубина равна 2, то указанная страница и те страницы и файлы, на которые она ссылается и так далее. Этот метод может вызываться параллельно в нескольких потоках.
Загрузка и обработка страниц (извлечение ссылок) должна выполняться максимально параллельно, с учетом ограничений на число одновременно загружаемых страниц (в том числе с одного хоста) и страниц, с которых загружаются ссылки.
Для распараллеливания разрешается создать до `downloaders + extractors` вспомогательных потоков.
Загружать и/или извлекать ссылки из одной и той же страницы в рамках одного обхода (download) запрещается.
Метод `close` должен завершать все вспомогательные потоки.
Для загрузки страниц должен применяться `Downloader`, передаваемый первым аргументом конструктора.
```java
public interface Downloader {
   public Document download(final String url) throws IOException;
}
```             
Метод `download` загружает документ по его адресу (URL).
Документ позволяет получить ссылки по загруженной странице:
```java
public interface Document {
   List<String> extractLinks() throws IOException;
}
```               
Ссылки, возвращаемые документом являются абсолютными и имеют схему http или https.
Должен быть реализован метод main, позволяющий запустить обход из командной строки

Командная строка `WebCrawler url [depth [downloads [extractors [perHost]]]]`
                
Для загрузки страниц требуется использовать реализацию CachingDownloader из пакета `info.kgeorgiy`.


## Домашнее задание 8. Параллельный запуск

Напишите класс `ParallelMapperImpl`, реализующий интерфейс `ParallelMapper`.

```java
public interface ParallelMapper extends AutoCloseable {
    <T, R> List<R> run(Function<? super T, ? extends R> f, List<? extends T> args throws InterruptedException;

    @Override
    void close() throws InterruptedException;
}
```
* Метод `run` должен параллельно вычислять функцию f на каждом из указанных аргументов (args).
* Метод `close` должен останавливать все рабочие потоки.
* Конструктор `ParallelMapperImpl(int threads)` создает `threads` рабочих потоков, которые могут быть использованы для распараллеливания.
* К одному `ParallelMapperImpl` могут одновременно обращаться несколько клиентов.
Задания на исполнение должны накапливаться в очереди и обрабатываться в порядке поступления.
* В реализации не должно быть активных ожиданий.


Добавьте класс `IterativeParallelism` так, чтобы он мог использовать `ParallelMapper`.
* Добавьте конструктор `IterativeParallelism(ParallelMapper)`
* Методы класса должны делить работу на threads фрагментов и исполнять их при помощи `ParallelMapper`.
* Должна быть возможность одновременного запуска и работы нескольких клиентов, использующих один `ParallelMapper`.
* При наличии `ParallelMapper` сам `IterativeParallelism` новые потоки создавать не должен.


## Домашнее задание 7. Итеративный параллелизм

Реализуйте класс `IterativeParallelism`, который будет обрабатывать списки в несколько потоков.

Должны быть реализованы следующие методы:
* `minimum(threads, list, comparator)` — первый минимум;
* `maximum(threads, list, comparator)` — первый максимум;
* `all(threads, list, predicate)` — проверка, что все элементы списка удовлетворяют предикату;
* `any(threads, list, predicate)` — проверка, что существует элемент списка, удовлетворяющий предикату.
* `filter(threads, list, predicate)` — вернуть список, содержащий элементы удовлетворяющие предикату;
* `map(threads, list, function)` — вернуть список, содержащий результаты применения функции;
* `join(threads, list)` — конкатенация строковых представлений элементов списка.

Во все функции передается параметр `threads` — сколько потоков надо использовать при вычислении. Вы можете рассчитывать, что число потоков не велико.
Не следует рассчитывать на то, что переданные компараторы, предикаты и функции работают быстро.
При выполнении задания нельзя использовать Concurrency Utilities.
Рекомендуется подумать, какое отношение к заданию имеют моноиды.


## Домашнее задание 6. Javadoc

Документируйте класс `Implementor` и сопутствующие классы с применением Javadoc.

* Должны быть документированы все классы и все члены классов, в том числе `private`.
* Документация должна генерироваться без предупреждений.
* Сгенерированная документация должна содержать корректные ссылки на классы стандартной библиотеки.

Для проверки, кроме исходного кода так же должны быть предъявлены:
* скрипт для генерации документации;
* сгенерированная документация.

Данное домашнее задание сдается только вместе с предыдущим. Предыдущее домашнее задание отдельно сдать будет нельзя.

__NB__: Вы можете полагаться в скриптах на то, что рядом с вашим репозиторием будет лежать репозиторий `java-advanced-2020`. В нём будут директории `lib` и `artifacts` с jar-файлами, которые можно использовать в процессе компиляции.


## Домашнее задание 5. JarImplementor

Создайте .jar-файл, содержащий скомпилированный Implementor и сопутствующие классы.

* Созданный .jar-файл должен запускаться командой `java -jar`.
* Запускаемый .jar-файл должен принимать те же аргументы командной строки, что и класс Implementor.
* Модифицируйте `Implementor` так, что бы при запуске с аргументами `-jar имя-класса файл.jar` он генерировал .jar-файл с реализацией соответствующего класса (интерфейса).

Для проверки, кроме исходного кода так же должны быть предъявлены:
* скрипт для создания запускаемого .jar-файла, в том числе, исходный код манифеста;
* запускаемый .jar-файл.

Данное домашнее задание сдается только вместе с предыдущим. Предыдущее домашнее задание отдельно сдать будет нельзя.


## Домашнее задание 4. Implementor

Реализуйте класс `Implementor`, который будет генерировать реализации классов и интерфейсов.

Аргумент командной строки: полное имя класса/интерфейса, для которого требуется сгенерировать реализацию.

* В результате работы должен быть сгенерирован java-код класса с суффиксом Impl, расширяющий (реализующий) указанный класс (интерфейс).
* Сгенерированный класс должен компилироваться без ошибок.
* Сгенерированный класс не должен быть абстрактным.
* Методы сгенерированного класса должны игнорировать свои аргументы и возвращать значения по умолчанию.
 
В задании выделяются три уровня сложности:
* Простой — Implementor должен уметь реализовывать только интерфейсы (но не классы). Поддержка generics не требуется.
* Сложный — Implementor должен уметь реализовывать и классы и интерфейсы. Поддержка generics не требуется.
* Бонусный — Implementor должен уметь реализовывать generic-классы и интерфейсы. Сгенерированный код должен иметь корректные параметры типов и не порождать UncheckedWarning.

_Примечание: В этом репозитории - решение сложного варианта._

## Домашнее задание 3. Студенты

Разработайте класс `StudentDB`, осуществляющий поиск по базе данных студентов.
Класс `StudentDB` должен реализовывать интерфейс `StudentGroupQuery`.
Каждый метод должен состоять из ровно одного оператора. При этом длинные операторы надо разбивать на несколько строк.

При выполнении задания следует обратить внимание на:
* Применение лямбда-выражений и потоков.
* Избавление от повторяющегося кода

## Домашнее задание 2. ArraySortedSet

Разработайте класс `ArraySet`, реализующие неизменяемое упорядоченное множество.

Класс `ArraySet` должен реализовывать интерфейс `NavigableSet`.

Все операции над множествами должны производиться с максимально возможной асимптотической эффективностью.

При выполнении задания следует обратить внимание на:
* Применение стандартных коллекций.
* Избавление от повторяющегося кода.


## Домашнее задание 1. Обход файлов


Разработайте класс `RecursiveWalk`, осуществляющий подсчет хеш-сумм файлов в директориях.

Входной файл содержит список файлов и директорий, которые требуется обойти. Обход директорий осуществляется рекурсивно.

Формат запуска `java RecursiveWalk <входной файл> <выходной файл>`

Входной файл содержит список файлов, которые требуется обойти.

Выходной файл должен содержать по одной строке для каждого файла. Формат строки:
`<шестнадцатеричная хеш-сумма> <путь к файлу>`

Для подсчета хеш-суммы используйте алгоритм FNV.

Если при чтении файла возникают ошибки, укажите в качестве его хеш-суммы 00000000.
Кодировка входного и выходного файлов — UTF-8.

Если родительская директория выходного файла не существует, то соответствующий путь надо создать.
Размеры файлов могут превышать размер оперативной памяти.

Пример 1

Входной файл
```
                        java/info/kgeorgiy/java/advanced/walk/samples/1
                        java/info/kgeorgiy/java/advanced/walk/samples/12
                        java/info/kgeorgiy/java/advanced/walk/samples/123
                        java/info/kgeorgiy/java/advanced/walk/samples/1234
                        java/info/kgeorgiy/java/advanced/walk/samples/1
                        java/info/kgeorgiy/java/advanced/walk/samples/binary
                        java/info/kgeorgiy/java/advanced/walk/samples/no-such-file
```                    
Выходной файл
```
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        2076af58 java/info/kgeorgiy/java/advanced/walk/samples/12
                        72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123
                        81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
                        00000000 java/info/kgeorgiy/java/advanced/walk/samples/no-such-file
```                    
Пример 2

Входной файл
```
                        java/info/kgeorgiy/java/advanced/walk/samples/binary
                        java/info/kgeorgiy/java/advanced/walk/samples
```                    
Выходной файл
```
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        2076af58 java/info/kgeorgiy/java/advanced/walk/samples/12
                        72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123
                        81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
```                    
При выполнении задания следует обратить внимание на:
* Дизайн и обработку исключений, диагностику ошибок.
* Программа должна корректно завершаться даже в случае ошибки.
* Корректная работа с вводом-выводом.
* Отсутствие утечки ресурсов.
