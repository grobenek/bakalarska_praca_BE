# bakalarska_praca_BE

Back-end časť pre bakalársku prácu s názvom:
Návrh aplikácie na zobrazenie meraných elektrických veličín uložených v databáze pomocou Modbus protokolu.

Použité technológie: Spring Boot, MySQL, InfluxDB

## InfluxDB

### Inicializácia a Konfigurácia

Pre správne fungovanie aplikácie je potrebné mať nastavenú InfluxDB databázu, organizáciu a bucket. Postupujte nasledovne:

1. **Inštalácia InfluxDB:** Ak ešte nemáte InfluxDB nainštalovaný, postupujte podľa inštrukcií na [inštaláciu InfluxDB]([https://docs.influxdata.com/influxdb/v2.0/get-started/installation/](https://docs.influxdata.com/influxdb/v2.7/)).

2. **Vytvorenie organizácie a bucketu:** Po inštalácii InfluxDB sa prihláste do [InfluxDB UI](http://localhost:8086) a vytvorte novú organizáciu a bucket pre vaše dáta.

## MySQL

### Inicializácia a Konfigurácia

Pre ukladanie určitých dát môže aplikácia používať aj MySQL databázu. Postupujte nasledovne:

1. **Inštalácia MySQL:** Ak ešte nemáte MySQL nainštalovaný, postupujte podľa inštrukcií na [inštaláciu MySQL]([https://dev.mysql.com/doc/mysql-installation-excerpt/5.7/en/](https://dev.mysql.com/doc/mysql-installation-excerpt/8.0/en/)).

2. **Naplňte dáta:** V projekte sa nachádza SQL dump súbor (`dump.sql`), ktorý obsahuje príklad dát pre vašu aplikáciu. Importujte tento súbor do vašej MySQL databázy.

### Spustenie Aplikácie

Jedným zo spôsobov je spustenie metódy main v triede szathmary.peter.bakalarka.BakalarkaApplication z vášho IDE.

Prípadne môžete použiť doplnok Spring Boot Maven takto:

```bash
./mvnw spring-boot:run
