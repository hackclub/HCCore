build:
	gradle build

view:
	jar tf build/libs/*.jar

run: build
	java -jar build/libs/*.jar

copy: build
	cp build/libs/*.jar ../server-root/plugins
