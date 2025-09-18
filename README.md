# 📈 Algorithmic Trading Platform

This repository contains a platform for creating, testing, and running algorithmic trading strategies on financial
markets.

---

## 🚀 Key Features

- ✅ **Strategy Creation** based on technical analysis indicators.
- 🤖 **Live Automated Trading** via broker API.
- 📊 **Backtesting** strategies on historical data — from broker API or local files.
- 🧩 **Flexible Architecture** — easy to add new strategies and customize parameters.

---

## 🧰 Technologies Used

- **Broker API**: [Tinkoff Invest API](https://github.com/Tinkoff/investAPI) — for market data and order execution.
- **Strategy Engine**: [ta4j](https://github.com/ta4j/ta4j) — powerful Java library for technical analysis and trading
  system development.

---

## ➕ Adding New Strategies

To add a new strategy:

1. Create a class implementing the interface: `ru.kcheranev.trading.core.strategy.factory.StrategyFactory`.
2. Register it as a Spring bean.

📌 **Example strategies** are available in the package:  
`ru.kcheranev.trading.core.strategy.factory`

---

## 📉 Backtesting

After starting the service, the backtesting web UI is available at:  
👉 [http://localhost:8080/ui/backtesting](http://localhost:8080/ui/backtesting)

Supported data sources:

- 📥 Historical candles via broker API.
- 🗃️ Local candle files (pre-formatted; can also be exported via the service API).

---

## 🛠️ Working with the API

The platform provides a REST API for managing strategies and trading sessions.  
Interactive API documentation (Swagger UI) is available at:  
👉 [http://localhost:8080/ui/swagger](http://localhost:8080/ui/swagger)

---

## 🧭 Step-by-Step Guide: Setup and Launch a Strategy

### 1. Add an Instrument

- Register the trading instrument in the instrument directory.
- Use the broker identifier (e.g., FIGI) obtained via the Tinkoff API.

### 2. Create Strategy Configuration

- Specify strategy parameters (implementation-dependent).
- Choose position sizing method (fixed lot, percentage of capital, etc.).

### 3. Start Trading Session

- Activate the session via API.
- The system automatically subscribes to candle data via the broker.
- On each new candle:
  - Strategy is recalculated.
  - Buy/sell signals are generated.
  - Orders are executed.
  - The cycle repeats.

---

## ⚙️ Configuration

Main settings are located in `application.yml`.

### Required Parameters:

| Parameter             | Description                               |
|-----------------------|-------------------------------------------|
| `DATASOURCE_URL`      | Database connection string                |
| `TEMP_FILE_DIRECTORY` | Directory for backtesting temporary files |
| `BROKER_TOKEN`        | Access token for Tinkoff Invest API       |

### Optional Parameters (Notifications):

```yaml
application:
  infra:
    notification:
      telegram:
        enabled: true          # Enable/disable Telegram notifications
        token: "YOUR_TOKEN"    # Telegram bot token
        chatId: "CHAT_ID"      # Chat ID to send notifications to
```

## ⚠️ **IMPORTANT**

- This project is under active development — API and architecture may change.
- **Always** perform thorough testing and backtesting before using in production.
- The author and contributors **are not liable** for any financial losses resulting from the use of this platform.