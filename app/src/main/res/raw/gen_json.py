import io
import json
import csv


data=open("chef.csv", "rb")
res = open("data_json.json", "w")
data = list(csv.reader(data, delimiter=';'))
ingredients=data[0]
ingredients_obj={}
for (i, val) in enumerate(ingredients):
	if i<7:
		description="Frutta"
	elif i>=7 and i<17:
		description="Verdura"
	elif i==17:
		description="Pesce"
	elif i==18:
		description="Carne"
	elif i>18 and i<=21:
		description="Grano e Riso"
	elif i==22 or i==25:
		description="Altro"
	else:
		description="Latticini"
	ingredients_obj[val]={"name" : val[:1].upper() + val[1:], "description" : description}	
			
	
		
dishes = data[1:]
levels = {"1" : [], "2" : []}
for dish in dishes:
	name = dish[0].split(" (")[0]
	level = str(dish[1])
	level_obj=levels[level]
	ingr = dish[2:]
	ingr = zip(ingr, ingredients)
	ingr = filter(lambda x: True if x[0]=="x" else False, ingr)
	ingr = map(lambda x: x[1], ingr)
	dish_obj = {"ingredients" : ingr, "name" : name}
	level_obj.append(dish_obj)
	levels[level]=level_obj
	
obj = {"levels" : levels, "ingredients" : ingredients_obj}
json.dump(obj, res, indent=4, sort_keys=True)
	