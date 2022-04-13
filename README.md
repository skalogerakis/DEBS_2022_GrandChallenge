# DEBS-2022 Grand Challenge
*Authors: Kalogerakis Stefanos, Antonis Papaioannou, Kostas Magoutis*

The  DEBS Grand Challenge 2022 focuses on real-time complex event processing of real-world high-volume tick data provided by Infront Financial Technology (https://www.infrontfinance.com/). The goal of the challenge is to efficiently compute specific trend indicators and detect patterns resembling those used by real-life traders to decide on buying or selling on the financial markets.

## Prerequisites

The proposed implementation was tested under the following tools and versions

| Tool      | Version | 
| :----:        |    :----:   | 
| Java      |  openjdk-8-jdk   |
| Maven      |  3.6.3   |
| Apache Flink      | 1.14.3       |
| Apache Kafka   | 2.12-3.1.0        |
| Redis   | 6.2.6        |

All of the aforementioned utilities can be easily installed using the `./manage.sh` script and the following command

    ./manage.sh install

***NOTE: It is highly recommended to use the script and not install the dependencies manually***
## Execution Instructions

The `./manage.sh` script completely automates the execution of the implementation. As stated in the prerequisites section, before executing the application for the first time install all the required dependencies using the following

    ./manage.sh install


Executing the complete implementation follows a similar pattern using the command
  
    ./manage.sh start

## Code Structure

This section briefly highlights on the structure of the implementation and its different components. The provided solution decouples the Data Ingestion-Reporting and Data Processing which allows more portability and flexibility.

### gRPC

Responsible for the Data Ingestion-Reporting of the application. Data Ingestion includes fetching the data using the provided gRPC client that communicates with the challenger API, and passes them to a specific Kafka Topic. For the reporting, the application subscribes in two different topics one for each of the desired queries, sends the results back to gRPC client and terminates the benchmarks as soon as it received all the expected messages.

### StockAnalysisApp-StockAnalysisOpt

Proposed solutions for the data processing using the Apache Flink Framework. The diffence between those solution lies in the scalability of the source operator. More specifically, `StockAnalysisApp` does not support parallelism in the source which leads to a much simpler design. In contrast, `StockAnalysisOpt` allows parallelism in source. The specific details and design choices will be reported in the paper submission.

## License

*Apache License 2.0*

