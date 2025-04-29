package com.example.fishtrip;

import com.fishtripplanner.Application;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.dto.UserMapper;
import com.fishtripplanner.mapper.macbook;
import com.fishtripplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
@SpringBootTest(
		classes = Application.class,
		properties = "spring.main.allow-bean-definition-overriding=true"
)


class FishtripApplicationTests {


	@Autowired
	macbook Macbook;

	@Test
	void crwr() {
		List<UserMapper> list = Macbook.selectAll();
		for(UserMapper m : list)
		{
			System.out.print(m.getUsername());
			System.out.print(" | ");
			System.out.print(m.getEmail());
			System.out.print(" | ");
			System.out.println(m.getId());
		}
	}

	}


