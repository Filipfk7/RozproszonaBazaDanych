1.---------------------------------------------------WPROWADZENIE---------------------------------------------------------

DatabaseNode jest implementacją węzła bazy danych, który jest w stanie odbierać żądania od klienta i wykonywać operacje na bazie danych.
Klient może wysyłać żądania przez socket TCP, a węzeł bazy danych jest w stanie obsłużyć te żądania i zwrócić odpowiednie dane.
Węzeł bazy danych jest skonfigurowany za pomocą opcji przekazanych w argumentach, w tym opcje rekordów, connectów do połączenia, oraz portu TCP.
Procesy tworzą sieć, w której dany proces jest połączony z co najmniej jednym innym procesem z sieci.
Sieć rozrasta się poprzez łączenie kolejnych węzłów i podłączanie ich do sieci.
Wszystkie procesy przechowują swoje dane jak i przyjmują zlecenia operacji klientów.

2. ------------------------------------------------OPIS IMPLEMENTACJI-----------------------------------------------------

Projekt składa się z jednej klasy głównej (DatabaseNode), 3 klas pomocniczych (ClientRequest, Node, Server),
3 enumów (CommunicationHelper, Operation, Option) oraz Interface'u (RequestHandler).

KLASY:

DatabaseNode -  Klasa "DatabaseNode" jest główną klasą programu, która implementuje serwer bazy danych.
W metodzie main, za pomocą argumentów przekazanych do programu, jest tworzona mapa opcji, rekord, serwery, oraz numer portu TCP.
Następnie, program tworzy gniazdo serwera i w pętli nasłuchuje na połączenia. Po otrzymaniu połączenia, tworzony jest obiekt "Node", który jest
przetwarzany zgodnie z żądaniem klienta i operacją przekazaną przez klienta. Jeśli operacja to "TERMINATE", gniazdo serwera jest zamykane,
a program kończy działanie.

ClientRequest - Klasa służy do tego aby odczytywać i zinterpretować żądanie klienta.

Node - Klasa "Node" jest klasą, która reprezentuje pojedynczy węzeł w sieci serwerów,
w ten sposób program może przetwarzać i zarządzać połączeniem z klientem oraz danymi przetwarzanymi w ramach tego połączenia.

ENMUY:

Operation - Enum zawiera typy "-operation", które mogą zostać wywołane przez klienta.
Każda stała implementuje metodę "handle" z interfejsu "RequestHandler", która jest odpowiedzialna za obsługę danego typu operacji.
Metody handle, przetwarzają żądania i korzystają z obiektu "Node" i dostępnych danych, aby wykonać operację, po wykonaniu zostaje wyświetlony output operacji.
Enum "Operation" zawiera także kilka pomocniczych metod takich jak:

 sendResponse - które służą do wysyłania odpowiedzi do klienta,
 askRelatedNodes - przeszukiwania innych węzłów w sieci w celu uzyskania danych
 searchEdgeValues -używana do przeszukiwania innych węzłów w sieci w celu uzyskania danych.
 Jej celem jest znalezienie wartości minimalnych lub maksymalnych w sieci.

Option -  reprezentuje opcje, jakie mogą zostać przekazane przez użytkownika podczas uruchamiania programu.
Każda stała enuma reprezentuje jedną z tych opcji. Enum zawiera trzy stałe: TCP_PORT, RECORD, CONNECT.
 Metoda statyczna jest używana do przekonwertowania ciągu tekstowego na odpowiednią stałą enuma.
 Mapa "textEnumMap" jest używana do przechowywania mapowania między ciągiem tekstowym a stałą enuma.

INTERFACY:

RequestHandler - do implementacji operation, aby wykorzystać ją do przetwarzania żądań od klientów.


3. ---------------------------------------------OPIS BUDOWY KOMUNIKATÓW---------------------------------------------------

Funkcja main() jest głównym punktem wejścia do programu, który odpowiada za inicjalizację węzła bazy danych i obsługę żądań klienta.
Klient podaje nam argumenty przy tworzeniu nowego węzła np.

java DatabaseNode -tcpport <numer portu TCP> -record <klucz>:<wartość>
 -connect <adres>:<port>

gdzie:

-tcpport <numer portu TCP> - określa numer portu TCP na którym dany węzeł sieci
oczekuje na połączenia od klientów.

• -record <klucz>:<wartość> - oznacza parę liczb całkowitych początkowo
przechowywanych w bazie na danym węźle, gdzie pierwsza to klucz a druga to wartość
związana z tym kluczem.

• [ -connect <adres>:<port> ] - oznacza listę innych węzłów już będących w sieci, z
którymi dany węzeł ma się połączyć i z którymi może się komunikować w celu wykonywania
operacji.


Rekordy, serwery, oraz porty TCP są pobierane i przypisywane do poszczególnych węzłów.
Następnie tworzone jest gniazdo serwera za pomocą klasy ServerSocket i przypisywany jest do podanego portu TCP.
Po tej operacji węzeł bazy danych jest gotowy do odbierania połączeń od klienta.
W momencie, gdy połączenie zostaje nawiązane, tworzony jest nowy obiekt Node z socketem, rekordami i connectami z poszczególnymi węzłami, które istnieją już w bazie.
Następnie, ciało żądania jest pobierane od klienta za pomocą metody getInput() z klasy CommunicationHelper i
jest konwertowane na obiekt klasy ClientRequest za pomocą metody convertToClientRequest().

Operacja, która ma zostać wykonana jest pobierana z obiektu klienta za pomocą metody getOperation(), a parametr operacji
jest pobierany za pomocą metody getParameter(). Następnie metoda handle() jest wywoływana na obiekcie klasy Operation,
której parametrem jest instancja klasy Node oraz parametr przekazany przez klienta.
Ta metoda pozwala na wykonanie następujących operacji w bazie danych:

1. set-value <klucz>:<wartość> : ustawienie nowej wartości (drugi parametr) dla klucza
będącego pierwszym parametrem. Wynikiem operacji jest komunikat OK jeśli operacja się
powiodła lub ERROR jeśli baza nie zawiera żadnej pary, w której występuje żądany klucz.

2. get-value <klucz> : pobranie wartości dla klucza będącego parametrem w bazie.
Wynikiem operacji jest komunikat składający się z pary <klucz>:<wartość> jeśli operacja
się powiodła lub ERROR jeśli baza nie zawiera żadnej pary, w której występuje żądany klucz.

3. find-key <klucz> : zlecenie wyszukania adresu i numeru portu węzła, na którym
przechowywany jest rekord o zadanym kluczu. Jeśli taki węzeł istnieje, odpowiedzią jest para
postaci <adres>:<port> identyfikująca ten węzeł lub komunikat ERROR jeśli żaden węzeł
takiego klucza nie posiada.

4. get-max : znalezienie największej wartości przypisanej do wszystkich kluczy w bazie.
Wynikiem operacji jest komunikat składający się z pary <klucz>:<wartość>.

5. get-min : znalezienie najmniejszej wartości przypisanej do wszystkich kluczy w bazie.
Wynikiem operacji jest komunikat składający się z pary <klucz>:<wartość>.

6. new-record <klucz>:<wartość> : zapamiętanie nowej pary klucz:wartość w miejsce pary
przechowywanej na węźle, do którego dany klient jest podłączony. Wynikiem tej operacji jest
komunikat OK.

7. terminate : powoduje odłączenie się węzła od sieci poprzez poinformowanie o tym fakcie
swoich sąsiadów oraz zakończenie pracy. Sąsiedzi węzła poinformowani o zakończeniu przez
niego pracy uwzględniają ten fakt w swoich zasobach i przestają się z nim komunikować.
Przed samym zakończeniem pracy węzeł odsyła do klienta komunikat OK.

4. ---------------------------------------------PRZEPŁYW KOMUNIKACJI------------------------------------------------------

W kodzie przepływ komunikacji odbywa się poprzez wykorzystanie gniazda sieciowego (ServerSocket) i gniazda klienta (Socket).
Program uruchamia serwer, który nasłuchuje na określonym porcie TCP (metoda getTcpPort) i czeka na połączenie od klienta.
Kiedy klient się łączy, serwer akceptuje połączenie i tworzy gniazdo klienta. Następnie serwer odbiera żądanie od klienta i
przekształca je na obiekt ClientRequest (metoda convertToClientRequest),
który zawiera informacje o operacji oraz parametrze (jeśli jest potrzebny).
Serwer wykonuje następnie odpowiednią operację (metoda handle) na podstawie otrzymanych danych i zwraca odpowiedź do klienta.
Po zakończeniu operacji, serwer zamyka gniazdo klienta i czeka na kolejne połączenie.
Jeśli klient wysłał żądanie TERMINATE, serwer kończy działanie i zamyka gniazdo serwera.

5. ------------------------------------------------PROTOKÓŁ TCP-----------------------------------------------------------

Program korzysta z protokołu TCP do komunikacji między klientem a serwerem.
Protokół TCP zapewnia niezawodność i wiarygodność transmisji danych poprzez kontrolę przepływu, potwierdzenie dostarczenia danych oraz korekcję błędów.
W kodzie, metoda getTcpPort() odpowiada za pobranie portu, na którym serwer ma nasłuchiwać połączeń TCP. Następnie, w metodzie main(),
serwer tworzy gniazdo typu ServerSocket na podanym porcie i nasłuchuje połączeń. Gdy połączenie zostanie nawiązane,
serwer odbiera dane od klienta i przetwarza je zgodnie z otrzymanymi żądaniami.

6. ------------------------------------------------UWAGI AUTORA-----------------------------------------------------------

Program po podłączeniu się kilku nodów bierze je szeregowo tzn.:

Jeżeli mamy 3 nody:

start java DatabaseNode -tcpport 9000 -record 1:10
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:3
start java DatabaseNode -tcpport 9002 -connect localhost:9000 -connect localhost:9001 -record 3:4

To po wykonaniu jakiejkolwiek operacji np.

java DatabaseClient -gateway localhost:9000 -operation get-max
java DatabaseClient -gateway localhost:9000 -operation get-min

Output powyższych dwóch linii jest równy 10 i 10.

java DatabaseClient -gateway localhost:9001 -operation get-max
java DatabaseClient -gateway localhost:9001 -operation get-min

Output powyższych dwóch linii jest już równy 10 i 3.

Program wywołując -gateway localhost:9001 widzi tylko te porty, które są pod nim czyli tylko 9000.

Wszystkie skrypty działają w pełni jednak outputy mogą być różne od tych poprawnych właśnie z tego względu.









