package com.dnd5e.wiki.controller.rest.model.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;

@Getter
public class Reaction implements Serializable{
	private static final long serialVersionUID = 1L;

	@XmlElement
	private String name;
	@XmlElement
	private String text;
	public Reaction() {
		
	}
	
	public Reaction(String name, String text) {
		this.name = name;
		this.text = text;
	}
}