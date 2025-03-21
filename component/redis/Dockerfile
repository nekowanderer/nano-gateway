FROM redis:7.4.1

COPY redis-cluster.conf /usr/local/etc/redis/redis-cluster.conf
RUN chmod +r /usr/local/etc/redis/redis-cluster.conf

COPY init-scripts/init-redis-cluster.sh /usr/local/bin/init-redis-cluster.sh
RUN chmod +x /usr/local/bin/init-redis-cluster.sh

CMD ["redis-server", "/usr/local/etc/redis/redis-cluster.conf"]