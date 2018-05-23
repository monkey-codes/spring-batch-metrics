package codes.monkey.batchstats

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

//@EnableBatchProcessing
@SpringBootApplication
class BatchStatsApplication {

    static void main(String[] args) {
//        SpringApplication.run BatchStatsApplication, args

        System.exit(SpringApplication.exit(SpringApplication.run(
                BatchStatsApplication.class, args)));
    }
}
