# DEBS-2022 Grand Challenge - Group 14
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


All of the aforementioned utilities can be easily installed using the `./manage.sh` script and the following command

    ./manage.sh install

***NOTE: It is highly recommended to use the script and not install the dependencies manually***
## Execution Instructions

The `./manage.sh` script completely automates the execution of the implementation. As stated in the prerequisites section, before executing the application for the first time install all the required software stack(utilities, processing platforms) using the following

    ./manage.sh install
    
Next step, is building the application from source code

    ./manage.sh build
    
Lastly, the starting the application follows a similar pattern using the command
  
    ./manage.sh start
    
When everything is done, in order to stop processing and processing platform use the 
    
    ./manage.sh stop

**NOTE: In case of an error or unexpected behavior after execution start, you must first use the stop command to terminate the running processes and start the execution once again**
### Optional Start Parameters

When executing the `manage.sh start` command different configuration parameters are provided to further customize the functionality of the existing application. The available parameters are

| Parameter      | Expected Value | Description | Default Value|
|   :----:   |    :----:   |   :----:   |    :----:   | 
| p  | int number | Parallelism of Flink Application | 1 |
| i  | number| Parameter to Calculate EMA | 38 |
| j  | number| Parameter to Calculate EMA | 100 |
| c  | int number| Checkpointing interval in minutes | None |
| q  | 1 or 2| Speficy the required queries for reporting. 1 for Q1, 2 for Q2 | Both Queries |

For example `./manage.sh start -p 2 -i 50 -j 90 -q 1`, would suggest that the parallelism in Flink application is 2, the EMA parameters for evaluation are 50 and 90, while we are interested in acquiring results only for the first query.

**NOTES:**
- In case either of the i, j are invalid then the application executes for the default values
- In case checkpointing parameter is not valid(not integer) then no checkpointing is performed. 
- The location where checkpoints are stored is **<repository\_dir>/flink_checkpoint**
- In case query option is invalid, both queries are evaluated. The application is set to evaluation mode.
- Parallelism option (if defined) must be a **valid option**. This option also adjusts the flink configurations accordingly such as there are sufficient task slots in the flink cluster to execute.
    
## Code Structure

This section briefly highlights on the structure of the implementation and its different components. The provided solution decouples the Data Ingestion-Reporting and Data Processing which allows more portability and flexibility.

### gRPC

Responsible for the Data Ingestion-Reporting of the application. Data Ingestion includes fetching the data using the provided gRPC client that communicates with the challenger API, and passes them to a specific Kafka Topic. For the reporting, the application subscribes in two different topics one for each of the desired queries, sends the results back to gRPC client and terminates the benchmarks as soon as it received all the expected messages.

### StockAnalysisApp

Proposed solution for the data processing aspect of the application using the Apache Flink Framework. This is implementation that currently executes in our application and under which our final evaluation scores occur.

| Version      | Throughput | Latency |
| :----:        |    :----:   | :----:   | 
| StockAnalysisApp      |  47.8953313765719  |  400.031743|

### StockAnalysisOpt

An evolved version of the running implementation of `StockAnalysisMap`. The main difference between those solutions lies in the scalability of the source operator. More specifically, `StockAnalysisApp` does not scale in the source operator and leads to a more straightforward implementation. In contrast, `StockAnalysisOpt` allows parallelism in source but requires much more sophisticated handling. The specific details and design choices will be reported in the paper submission.

**NOTE: This version still under extensive evaluation and optimization process, so it is currently not available for execution**


## License

*Apache License 2.0*

