package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
}
