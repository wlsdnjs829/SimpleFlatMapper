[![Build Status](https://travis-ci.org/arnaudroger/SimpleFlatMapper.svg?branch=master)](https://travis-ci.org/arnaudroger/SimpleFlatMapper)
[![Coverage Status](https://img.shields.io/coveralls/arnaudroger/SimpleFlatMapper.svg)](https://coveralls.io/r/arnaudroger/SimpleFlatMapper)

SimpleFlatMapper
========
Fast and Easy mapping from database to POJO. 
A super lightweight no configuration ORM alternative to iBatis or Hibernate.

- Compatible with Java 6, 7 and 8. 
- [Lambda Ready](#jdbcmapper).
- easy to integrate with [Spring JdbcTemplate](#jdbctemplate). 
- [Osgi](#osgisupport) ready.
- [QueryDSL Jdbc support](#querydsl-jdbc)

Design
========
- no configuration
- low foot print
- use plain jdbc
- no external library needed
- respect final fields
- support asm generation for max performance

What it does not do
-------
- no query generation
- no insert/update
- no caching
- no object lifecycle

Why?
-------

[Mapping Landscape](https://github.com/arnaudroger/SimpleFlatMapper/wiki/Mapping-Landscape)

### Performance

Ibatis and hibernate have very expensive injection mechanism. On the hsqldb in memory the markup for a medium size query is [400%](#in-mem-hsqldb) for both. 

BeanPropertyRowMapper is very slow.

Sfm is as fast as it can using asm generation. Even if you don't use asm it is still a lot faster. 


### API intrusiveness

Ibatis provide the same kind of functionality put it forces you to use it's query mechanism and mask the jdbc api. 
Sfm just focus on the mapping from a [ResultSet](#jdbcmapper). You can manage the query the way you want. You can use [JdbcTemplate](#jdbctemplate), even use it in an Hibernate session via the doWork method.

Samples
========

JdbcMapper
---------

```java

public class MyDao {
    private final JdbcMapper<MyObject> mapper = 
    	JdbcMapperFactory.newInstance().newMapper(MyObject.class);

    public void writeAllObjectTo(Writer writer, Connection conn) throws SQLException {
        try (PreparedStatement ps = 
        		conn.prepareStatement("select id, email, my_property from MyTable")) {
	        try (ResultSet rs = ps.executeQuery()){
	            mapper.forEach(rs, (o) -> writer.append(o.toString()).append("\n"));
	        }
        }
    }
}
```

JdbcTemplate
-----

See [JdbcTemplateMapperFactoryTest](/src/test/java/org/sfm/jdbc/spring/JdbcTemplateMapperFactoryTest.java) for more examples.

```java
class MyDao {
	private final JdbcTemplateMapper<DbObject> mapper = 
		JdbcTemplateMapperFactory.newInstance().newMapper(DbObject.class);
		
	public void doSomething() {		
		List<DbObject> results = template.query(DbHelper.TEST_DB_OBJECT_QUERY, mapper);
	}
	
	public void doSomethingElse() {		
		 template
		 	.query(TEST_DB_OBJECT_QUERY, 
		 		mapper.newResultSetExtractor((o) -> System.out.println(o.toString())));
	}
}
```

OsgiSupport
------
The Osgi support just expose a service that will deal with the classloading wizardry needed to generate bytecode.

```java
class MyService {

	@Reference
	JdbcMapperService jdbcMapperService;
	
	volatile JdbcMapper mapper;
	
	@Activate
	public void activate() {
		mapper = jdbcMapperService.newFactory().newMapper(DbObject.class);
	}
}
```

QueryDSL Jdbc
------

```java
SQLQuery sqlquery = new SQLQueryImpl(conn, new HSQLDBTemplates());
try {
	return sqlquery
		.from(qTestDbObject)
		.where(qTestDbObject.id.eq(1l))
		.list(new QueryDslMappingProjection<DbObject>(DbObject.class, 
				qTestDbObject.id,
				qTestDbObject.name, 
				qTestDbObject.email, 
				qTestDbObject.creationTime, 
				qTestDbObject.typeName, 
				qTestDbObject.typeOrdinal ));
} finally {
	conn.close();
}
```

Property Mapping
========

the mapper will assume a column name from the database will be matching the property name ignoring the case and underscores.

ie:
```
- my_property => myProperty
- myproperty => myProperty
```


Value Injection
------

The JdbcMapper supports
- constructor injection - needs asm to get the parameters name -
- setter injection
- field injection
It looks for injection on that order and if asm is present will generate optimised asm version.


```sql
create table MyTable {
	id bigint,
	email varchar(256),
	my_property int
}
```

```java
public class MyObject {
	private final long id;
	private final String email;
	private final int myProperty;
	
	public MyObject(long id, String email,  int myProperty) {
		this.id = id;
		this.email = email;
		this.myProperty = myProperty;
	}

	public long getId() { return id; }
	public String getEmail() { return email; }
	public int getProperty() { return myProperty; }
	
	public String toString() { ... }
}
```

Inner object mapping
-------

It also supports complex object injection via constructor, field or setter.

```java
public class OuterObject {
	String id;
	MyObject subObject;
}
```

```sql
select id, sub_object_id, sub_object_email, sub_object_my_property
```


List Mapping
-------

And list mapping in an object or at first level.

```java
public class ListObject {
	String id;
	List<MyObject> subObjects;
}
```

```sql
select id, 
	sub_objects_0_id, sub_objects_0_email, sub_objects_0_my_property, 
	sub_objects_1_id, 
	sub_objects_3_id   
```

Performance
========
See [orm-benchmarks](https://github.com/arnaudroger/orm-benchmark) for more details.

Some of the result that seem odd look to be linked to the optimizer, I'm trying to get all [JMH](http://openjdk.java.net/projects/code-tools/jmh/) to increase consitency.

BeanPropertyRowMapper is not in the benchmark because it makes the benchmark run time to last more than an overnight run. I'll try to add to jmh as it uses a timebound benchmark.

Mock Connection
-------

|Rows|SfmStatic|Sfm|SfmNoAsm|Roma|Sql2O|Hibernate|MyBatis|
|------:|------:|------:|-------:|-------:|------:|----:|----:|
|1|9%|18%|49%|4%|1009%|7561%|3457%|
|10|12%|18%|186%|16%|989%|7265%|6363%|
|100|3%|8%|318%|26%|1004%|7232%|10365%|
|1000|24%|63%|344%|28%|1042%|6831%|9264%|

In mem HsqlDb
-------

|Rows|SfmStatic|Sfm|SfmNoAsm|Roma|Sql2O|Hibernate|MyBatis|
|------:|------:|------:|-------:|-------:|------:|----:|----:|
|1|4%|11%|3%|13%|49%|186%|105%|
|10|5%|8%|8%|13%|50%|241%|207%|
|100|8%|10%|19%|16%|65%|400%|531%|
|1000|14%|14%|29%|22%|79%|524%|714%|

Local Mysql
-------

|Rows|SfmStatic|Sfm|SfmNoAsm|Roma|Sql2O|Hibernate|MyBatis|
|------:|------:|------:|-------:|-------:|------:|----:|----:|
|1|0%|1%|1%|6%|12%|176%|123%|
|10|0%|1%|2%|111%|137%|169%|125%|
|100|1%|1%|2%|5%|12%|52%|72%|
|1000|3%|1%|6%|8%|27%|116%|188%|


Maven dependency
======

```xml
		<dependency>
			<groupId>com.github.arnaudroger</groupId>
			<artifactId>simpleFlatMapper</artifactId>
			<version>0.9.3</version>
		</dependency>
```

