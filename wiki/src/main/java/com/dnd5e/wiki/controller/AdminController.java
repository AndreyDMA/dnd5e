package com.dnd5e.wiki.controller;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dnd5e.wiki.model.creature.Ability;
import com.dnd5e.wiki.model.creature.Action;
import com.dnd5e.wiki.model.creature.ActionType;
import com.dnd5e.wiki.model.creature.Alignment;
import com.dnd5e.wiki.model.creature.Creature;
import com.dnd5e.wiki.model.creature.CreatureRace;
import com.dnd5e.wiki.model.creature.CreatureSize;
import com.dnd5e.wiki.model.creature.CreatureTrait;
import com.dnd5e.wiki.model.creature.CreatureType;
import com.dnd5e.wiki.model.creature.DamageType;
import com.dnd5e.wiki.model.creature.Dice;
import com.dnd5e.wiki.model.creature.Language;
import com.dnd5e.wiki.model.creature.SavingThrow;
import com.dnd5e.wiki.model.creature.Skill;
import com.dnd5e.wiki.model.creature.SkillType;
import com.dnd5e.wiki.model.creature.State;
import com.dnd5e.wiki.model.spell.MagicSchool;
import com.dnd5e.wiki.model.spell.Spell;
import com.dnd5e.wiki.repository.CreatureRaceRepository;
import com.dnd5e.wiki.repository.CreatureRepository;
import com.dnd5e.wiki.repository.LanguagesRepository;
import com.dnd5e.wiki.repository.SpellRepository;

@Controller
@RequestMapping("/admin/")
public class AdminController {
	private static final List<String> statusNames = Arrays.asList("Спасброски", "Навыки", "Иммунитет к урону",
			"Сопротивление к урону", "Сопротивление урону", "Уязвимость к урону", "Иммунитет к состоянию",
			"Иммунитет к состояниям", "Чувства");
	
	@Autowired
	private SpellRepository spellRepository;
	@Autowired
	private LanguagesRepository languagesRepository;
	@Autowired
	private CreatureRaceRepository creatureRaceRepository;
	@Autowired
	private CreatureRepository creatureRepository;
	
	@GetMapping("/spell/add")
	public String getSpellAddForm(Model model) {
		return "/admin/addSpell";
	}

	@PostMapping("/spell/add")
	public String addSpell(String spellText) {

		Spell spell = new Spell();
		try (LineNumberReader reader = new LineNumberReader(new StringReader(spellText))) {
			String header = reader.readLine();
			if (header.contains("[")) {
				int start = header.indexOf('[');
				int end = header.indexOf(']');
				String englishName = header.substring(start + 1, end);
				spell.setEnglishName(englishName);
				spell.setName(header.substring(0, start).trim().toUpperCase());
			} else {
				spell.setName(header.trim().toUpperCase());
			}
			List<Spell> spells = spellRepository.findByName(spell.getName());
			if (spells != null && !spells.isEmpty()) {
				spell = spells.get(0);
			}
			String text = reader.readLine();
			String[] levelAndSchool = text.split(",");
			if (levelAndSchool[0].startsWith("Заговор")) {
				spell.setLevel((byte)0);
			} else {
				byte level = Byte.valueOf(levelAndSchool[0].split(" ")[0]);
				spell.setLevel(level);
			}
			if (levelAndSchool[1].contains("ритуал")) {
				spell.setRitual(true);
				levelAndSchool[1] = levelAndSchool[1].replace("ритуал", "");
			} else {
				spell.setRitual(false);
			}
			spell.setSchool(MagicSchool.getMagicSchool(levelAndSchool[1].trim()));
			text = reader.readLine();
			spell.setTimeCast(text.split(":")[1].trim());
			text = reader.readLine();
			spell.setDistance(text.split(":")[1].trim());
			text = reader.readLine();
			String components = text.split(":")[1].trim();
			String[] componetnsArray = components.split(",");
			for (int i = 0; i < componetnsArray.length; i++) {
				if (componetnsArray[i].trim().equals("В")) {
					spell.setVerbalComponent(true);
				}
				if (componetnsArray[i].trim().equals("С")) {
					spell.setSomaticComponent(true);
				}
				if (componetnsArray[i].trim().startsWith("М")) {
					spell.setMaterialComponent(true);
					if (componetnsArray[i].trim().contains("(")) {
						String materials = "";
						componetnsArray[i] = componetnsArray[i].replace("М (", "");
						for (int j = i; j < componetnsArray.length; j++) {
							if (componetnsArray[j].trim().contains(")")) {
								materials += componetnsArray[j].trim().substring(0,
										componetnsArray[j].trim().length() - 1);
								break;
							} else {
								materials += componetnsArray[j].trim() + ", ";
							}

						}
						spell.setAdditionalMaterialComponent(materials);
					}
				}
			}
			text = reader.readLine();
			String duration = text.split(":")[1].trim();
			spell.setDuration(duration);
			StringBuilder sb = new StringBuilder();
			while ((text = reader.readLine()) != null && (!text.startsWith("На более высоких"))) {
				if (text.endsWith("-")) {
					text = text.substring(0, text.length() - 1);
				} else {
					text += " ";
				}
				sb.append(text);
			}
			spell.setDescription(sb.toString());
			if (text != null) {
				if (text.endsWith("-")) {
					text = text.substring(0, text.length() - 1);
				}
				sb = new StringBuilder(text.replace("На более высоких уровнях.", "").replace("На более высоких кругах.",""));
				while ((text = reader.readLine()) != null) {
					if (text.endsWith("-")) {
						text = text.substring(0, text.length() - 1);
					} else {
						text += " ";
					}
					sb.append(text);
				}
				spell.setUpperLevel(sb.toString());
			}
			spellRepository.save(spell);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "/admin/addSpell";
	}

	@GetMapping("/creature/add")
	public String getMonsterForm(Model model) {
		model.addAttribute("monster", new Creature());
		return "/admin/parseMonster";
	}
	

	@PostMapping("/creature/add")
	public String createCreature(Model model, String description) {
		Creature creature = new Creature();
		try (LineNumberReader reader = new LineNumberReader(new StringReader(description))) {
			String name = reader.readLine();
			int index = name.indexOf('[');
			if (index > 0) {
				String otherName = name.substring(index + 1, name.indexOf(']'));
				creature.setName(name.substring(0, index).trim());
				creature.setEnglishName(otherName.trim());
			} else {
				creature.setName(name.trim());
			}
			if (creatureRepository.findByName(creature.getName())!= null) {
				return "parseMonster";
			}
			String sizeTypeAligmentString = reader.readLine();
			String[] sizeTypeAligment = sizeTypeAligmentString.split(",");
			String[] sizeType = sizeTypeAligment[0].split("\\s");

			// размер
			String size = sizeType[0];
			creature.setSize(CreatureSize.parse(size.trim()));
			// тип
			String type = sizeType[1];
			creature.setType(CreatureType.parse(type.trim()));
			if (sizeType.length > 2) {
				CreatureRace race = creatureRaceRepository.findByName(sizeType[2].trim());
				if (race == null) {
					race = new CreatureRace();
					race.setName(sizeType[2].trim());
					race = creatureRaceRepository.save(race);
				}
				creature.setRaceName(race.getName());
				creature.setRaceId(race.getId());
			}
			// мировозрение
			if (sizeTypeAligment.length > 1) {
				creature.setAlignment(Alignment.parse(sizeTypeAligment[1].trim()));
			}
			// доспех
			String armorString = reader.readLine();
			if (armorString.contains("Класс Доспеха")) {
				String[] armor = armorString.split(" ");
				byte ac = Byte.parseByte(armor[2].trim());
				creature.setAC(ac);
			}

			// Хиты
			String hitsString = reader.readLine();
			if (hitsString.contains("Хиты")) {
				String[] hits = hitsString.split("\\s");
				creature.setAverageHp(Short.parseShort(hits[1].trim()));
				hits[2] = hits[2].replace("(", "");
				String[] dices = hits[2].split("к");
				creature.setCountDiceHp(Short.parseShort(dices[0].trim()));
				dices[1] = dices[1].replace(")", "");
				int dice = Short.parseShort(dices[1].trim());
				creature.setDiceHp(Dice.parse(dice));
				if (hits.length > 3) {
					hits[3] = hits[3].replaceAll("[^0-9]", "").trim();
					if (!hits[3].isEmpty()) {
						creature.setBonusHP(Short.parseShort(hits[3]));
					} else if (hits.length > 4) {
						hits[4] = hits[4].replaceAll("[^0-9]", "").trim();
						creature.setBonusHP(Short.parseShort(hits[4]));
					}
				}
			}

			// Скорость
			String speedString = reader.readLine();
			if (speedString.contains("Скорость")) {
				String[] speeds = speedString.split(",");
				speeds[0] = speeds[0].replaceAll("[^0-9]", "");
				creature.setSpeed(Short.parseShort(speeds[0].trim()));
				for (int i = 1; i < speeds.length; i++) {
					String otherSpeed = speeds[i];
					if (otherSpeed.contains("летая") || otherSpeed.contains("полёт")) {
						otherSpeed = otherSpeed.replaceAll("[^0-9]", "").trim();
						creature.setFlySpeed(Short.parseShort(otherSpeed));
					} else if (otherSpeed.contains("плавая")) {
						otherSpeed = otherSpeed.replaceAll("[^0-9]", "").trim();
						creature.setSwimmingSpped(Short.parseShort(otherSpeed));
					} else if (otherSpeed.contains("лазая") || otherSpeed.contains("взбирание")) {
						otherSpeed = otherSpeed.replaceAll("[^0-9]", "").trim();
						creature.setClimbingSpeed(Short.parseShort(otherSpeed));
					}
				}
			}
			reader.readLine();
			String abilitesString = reader.readLine();
			abilitesString = abilitesString.replace("–", "+");
			abilitesString = abilitesString.replaceAll("\\(\\s?\\+[0-9]{1,2}\\s?\\)", "");
			String[] abilites = abilitesString.split("\\s+");
			creature.setStrength(Byte.valueOf(abilites[0]));
			creature.setDexterity(Byte.valueOf(abilites[1]));
			creature.setConstitution(Byte.valueOf(abilites[2]));
			creature.setIntellect(Byte.valueOf(abilites[3]));
			creature.setWizdom(Byte.valueOf(abilites[4]));
			creature.setCharisma(Byte.valueOf(abilites[5]));

			// навыки
			String skillText = "";
			String currentSkill = null;
			String token = null;
			do {
				token = reader.readLine();
				boolean startSkill = false;
				for (String status : statusNames) {
					if (token.startsWith(status)) {
						startSkill = true;

						if (!status.equals(currentSkill)) {
							if (currentSkill != null) {
								parseSkill(currentSkill, skillText, creature);
							}
							currentSkill = status;
						}
						skillText = token;
						break;
					}
				}
				if (!startSkill) {
					skillText += " " + token;
				}
			} while (!token.startsWith("Чувства"));
			skillText = token;

			// чувства
			skillText = skillText.replace("Чувства ", "");
			token = reader.readLine();
			if (!token.startsWith("Языки")) {
				skillText += token;
				creature.setVision(skillText);
				skillText = reader.readLine();
			} else {
				creature.setVision(skillText);
				skillText = token;
			}

			// языки
			token = reader.readLine();
			if (!token.startsWith("Опасность")) {
				skillText += " " + token;
				if (skillText.startsWith("Языки")) {
					parseLanguage(skillText, creature);
				}
			} else {
				parseLanguage(skillText, creature);
				skillText = token;
			}

			// Опасность
			if (!skillText.startsWith("Опасность")) {
				skillText = reader.readLine();
			}
			skillText = skillText.replace("Опасность ", "").trim();
			String[] crAndExp = skillText.split(" ");
			creature.setChallengeRating(crAndExp[0].trim());
			creature.setExp(Integer.valueOf(crAndExp[1].replaceAll("[^0-9]", "").trim()));

			// Фиты
			CreatureTrait feat = new CreatureTrait();
			List<CreatureTrait> feats = new ArrayList<>();
			boolean newFeet = true;
			do {
				skillText = reader.readLine();
				if (newFeet) {
					int nameIndex = skillText.indexOf(".");
					if (nameIndex != -1) {
						String nameAction = skillText.substring(0, nameIndex);
						feat.setName(nameAction);
						newFeet = false;
						description = skillText.substring(nameIndex + 2);
					} else {
						if (description.endsWith("-")) {
							description = description.substring(0, description.length() - 1);
							description += skillText;
						} else {
							description += " " + skillText;
						}
						newFeet = false;
					}
				} else {
					if (description.endsWith("-")) {
						description = description.substring(0, description.length() - 1);
						description += skillText;
					} else {
						description += " " + skillText;
					}

				}
				if (skillText.endsWith(".")) {
					feat.setDescription(description);
					feats.add(feat);
					feat = new CreatureTrait();
					newFeet = true;
				}
			} while (!skillText.trim().equals("Действия"));
			creature.setFeats(feats);

			// Действия
			List<Action> actions = new ArrayList<>();
			boolean newAction = true;
			Action action = new Action();
			action.setActionType(ActionType.ACTION);
			description = "";
			do {
				skillText = reader.readLine();
				if (skillText == null) {
					break;
				}
				if (newAction) {
					int nameIndex = skillText.indexOf(".");
					if (nameIndex != -1) {
						String nameAction = skillText.substring(0, nameIndex);
						action.setName(nameAction);
						newAction = false;
						description = skillText.substring(nameIndex + 2);
					} else {
						if (description.endsWith("-")) {
							description = description.substring(0, description.length() - 1);
							description += skillText;
						} else {
							description += " " + skillText;
						}
						newAction = false;
					}
				} else {
					if (description.endsWith("-")) {
						description = description.substring(0, description.length() - 1);
						description += skillText;
					} else {
						description += " " + skillText;
					}

				}
				if (skillText.endsWith(".")) {
					action.setDescription(description);
					actions.add(action);
					action = new Action();
					action.setActionType(ActionType.ACTION);
					newAction = true;
				}
			} while (skillText != null);
			creature.setActions(actions);

		} catch (IOException e) {
			e.printStackTrace();
		}
		creatureRepository.save(creature);
		return "/admin/parseMonster";
	}

	private void parseLanguage(String skillText, Creature monster) {
		List<Language> languageList = new ArrayList<>();
		skillText = skillText.replace("Языки ", "");
		String[] languages = skillText.split(",");
		for (String lang : languages) {
			Language language = languagesRepository.findByName(lang.trim());
			if (language == null) {
				language = new Language();
				language.setName(lang.trim());
				Language save = languagesRepository.save(language);
				language.setId(save.getId());
			}
			languageList.add(language);
		}
		monster.setLanguages(languageList);
	}

	private void parseSkill(String currentSkill, String skillText, Creature monster) {
		switch (currentSkill) {
		case "Спасброски": {
			List<SavingThrow> savingThrows = new ArrayList<>();
			skillText = skillText.replace("Спасброски ", "");
			String[] savingThrowsPair = skillText.split(",");
			for (String string : savingThrowsPair) {
				String[] parts = string.trim().split(" ");
				SavingThrow savingThrow = new SavingThrow();
				savingThrow.setAbility(Ability.parseShortName(parts[0].trim()));
				savingThrow.setBonus(Byte.parseByte(parts[1].trim()));
				savingThrows.add(savingThrow);
			}
			monster.setSavingThrows(savingThrows);
			break;
		}
		case "Навыки": {
			List<Skill> skillsList = new ArrayList<>();
			skillText = skillText.replace("Навыки ", "");
			String[] skills = skillText.split(",");
			for (String string : skills) {
				Skill skill = new Skill();
				String[] skillEl = string.trim().split("\\+");
				skill.setType(SkillType.parse(skillEl[0].trim()));
				skill.setBonus(Byte.parseByte(skillEl[1].trim()));
				skillsList.add(skill);
			}
			monster.setSkills(skillsList);
			break;
		}
		case "Иммунитет к урону": {
			skillText = skillText.replace("Иммунитет к урону ", "");
			List<DamageType> damageList = getDamageTypes(skillText);
			monster.setImmunityDamages(damageList);
			break;
		}
		case "Сопротивление к урону":
		case "Сопротивление урону": {
			skillText = skillText.replace("Сопротивление к урону ", "");
			skillText = skillText.replace("Сопротивление урону ", "");
			List<DamageType> damageList = getDamageTypes(skillText);
			monster.setResistanceDamages(damageList);
			break;
		}
		case "Уязвимость к урону": {
			skillText = skillText.replace("Уязвимость к урону ", "");
			List<DamageType> damageList = getDamageTypes(skillText);
			monster.setVulnerabilityDamages(damageList);
			break;
		}
		case "Иммунитет к состоянию":
			skillText = skillText.replace("Иммунитет к состоянию ", "");
		case "Иммунитет к состояниям": {
			skillText = skillText.replace("Иммунитет к состояниям ", "");
			String[] stateTypes = skillText.split(",");
			List<State> immunityStateList = new ArrayList<>();
			for (String state : stateTypes) {
				immunityStateList.add(State.parse(state.trim()));
			}
			monster.setImmunityStates(immunityStateList);
			break;
		}
		}
	}

	private List<DamageType> getDamageTypes(String skillText) {
		List<DamageType> immunityDamageList = new ArrayList<>();
		String[] partDamages = null;
		if (skillText.contains(";")) {
			partDamages = skillText.split(";");
			skillText = partDamages[0];
		}

		String[] damageTypes = skillText.split(",");

		for (String damageType : damageTypes) {
			immunityDamageList.add(DamageType.parse(damageType.trim()));
		}
		if (partDamages != null) {
			immunityDamageList.add(DamageType.parse(partDamages[1].trim()));
		}
		return immunityDamageList;
	}
}