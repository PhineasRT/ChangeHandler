FROM java
MAINTAINER Jatin Shridhar <shridhar.jatin@gmail.com>

RUN mkdir -p /opt/changeHandler
WORKDIR /opt/changeHandler

COPY ./target/change-handler-1.0-SNAPSHOT.jar /opt/changeHandler

CMD java -cp change-handler-1.0-SNAPSHOT.jar com.jk.changehandler.App -t Messages -s arn:aws:dynadb:us-east-1:467623578459:table/Messages/stream/2015-11-15T08:57:12.124
