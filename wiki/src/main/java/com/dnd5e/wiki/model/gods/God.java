package com.dnd5e.wiki.model.gods;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dnd5e.wiki.model.creature.Alignment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Entity
@Table(name = "gods")
public class God {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String commitment;
	
	@Enumerated(EnumType.STRING)
	private GodSex sex;
	
	@Enumerated(EnumType.STRING)
	Alignment aligment;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	private String symbol;

	@ElementCollection(targetClass=Domain.class)
    @CollectionTable(name="god_domains")
	@Enumerated(EnumType.STRING)
	private List<Domain> domains;
}
