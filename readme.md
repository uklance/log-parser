# Notes

- I used JDBCTemplate from spring-jdbc because it was quick to get setup (JPA/Mybatis etc would've taken too long)
- I used JsonParser from jackson/fasterxml because it supports streaming so can handle large files
- I ensure that each id is always allocated the same thread by using a modulous of the id's hashCode()
