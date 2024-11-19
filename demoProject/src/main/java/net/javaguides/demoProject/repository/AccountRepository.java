package net.javaguides.demoProject.repository;

import net.javaguides.demoProject.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account,Integer> {

    @Query("from Account where Email = :email and pass = :password")
    public Account login(@Param("username") String username, @Param("password") String password);
}
