package com.dnd5e.wiki.model.hero.race;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.dnd5e.wiki.model.hero.classes.Feature;

import lombok.Data;

@Entity
@Table(name = "sub_races")
@Data
public class SubRace {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@OneToMany
	@JoinColumn(name = "race_id")
	List<Feature> features;
	
	private byte strength;
	private byte dexterity;
	private byte constitution;
	private byte intellect;
	private byte wizdom;
	private byte charisma;
}
