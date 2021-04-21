FROM 192.168.6.110:5000/alpine_with_encoding:3.9

LABEL maintainer="seungjin_lee2@tmax.co.kr"

ADD ./hyperesm /home/tmax/hyperesm
ADD ./start.sh /home/tmax/
RUN chmod 775 -R /home/tmax/hyperesm ; \
	chmod 775 /home/tmax/start.sh ; \
	sed -i 's/\r$//' /home/tmax/start.sh ; \
	apk update ; \
	apk upgrade ; \
	apk add tzdata

CMD ["/bin/sh", "-c", "/home/tmax/start.sh"]
