drop keyspace GROW;

create keyspace GROW
    with placement_strategy = 'org.apache.cassandra.locator.SimpleStrategy'
    and  strategy_options = {replication_factor:1};

use GROW;

create column family strings
    with key_validation_class = 'UTF8Type'
    and comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type';

create column family assessments
    with key_validation_class = 'UTF8Type'
    and comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type';

create column family training
    with key_validation_class = 'UTF8Type'
    and comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type';
