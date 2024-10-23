# ------------------------------------------------------------------------------
# Makefile
# ------------------------------------------------------------------------------

PROJECT     := sudoku
MAIN_CLASS  := com.danihelis.sudoku.Main
ARGS        :=

# Executables ------------------------------------------------------------------
JAVAC       := javac
JAVA        := java
JAR         := jar

# Configuration ----------------------------------------------------------------
JAVAC_ARGS  := -Xlint:unchecked -Xlint:deprecation
JAVA_ARGS   :=

# Directories ------------------------------------------------------------------
SRC_DIR     := src
BLD_DIR     := build
DST_DIR     := dist

# Files ------------------------------------------------------------------------
SRC_FILES   := $(shell find $(SRC_DIR) -name '*.java')
CLS_FILES   := $(patsubst %.java,$(BLD_DIR)/%.class,$(notdir $(SRC_FILES)))
JAR_FILE    := $(DST_DIR)/$(PROJECT).jar

# Macros -----------------------------------------------------------------------
define ECHO
  env echo -e "\e[0;3$(1)m`printf '%11s' [$(2)]`\e[0m $(3) \e[1;37m$(4)\e[0m"
endef
define RMDIR
  if [ -e "$(1)" ]; then $(call ECHO,3,$@,Removing directory $(1)); rm -r $(1); fi
endef
define RMFILE
  if [ -e "$(1)" ]; then $(call ECHO,3,$@,Removing $(1)); rm $(1); fi
endef

# Targets ----------------------------------------------------------------------
.PHONY: run again jar compile clean mrproper

run: $(JAR_FILE)
	@$(call ECHO,4,$(JAVA),Executing $^,$(ARGS))
	@$(JAVA) $(JAVA_ARGS) -jar $^ $(ARGS)

again: mrproper run

jar: $(JAR_FILE)

compile: $(CLS_FILES)

$(CLS_FILES): $(SRC_FILES)
	@$(call ECHO,2,$(JAVAC),Compiling source files,$(JAVAC_ARGS))
	@$(JAVAC) $^ -d $(BLD_DIR) $(JAVAC_ARGS)

$(JAR_FILE): $(CLS_FILES)
	@$(call ECHO,2,$(JAR),Building $@)
	@$(JAR) cfe $@ $(MAIN_CLASS) -C $(BLD_DIR) .

clean:
	@$(call RMDIR,$(BLD_DIR))

mrproper: clean
	@$(call RMDIR,$(DST_DIR))
	@$(call RMFILE,$(DESKTOP_FILE))
