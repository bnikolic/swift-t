/*
 * Copyright 2013 University of Chicago and Argonne National Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package exm.stc.frontend;

import java.util.ArrayList;
import java.util.List;

import exm.stc.common.exceptions.DoubleDefineException;
import exm.stc.common.exceptions.STCRuntimeError;
import exm.stc.common.exceptions.UserException;
import exm.stc.common.lang.Types;
import exm.stc.common.lang.Types.FunctionType;
import exm.stc.common.lang.Types.Type;
import exm.stc.common.lang.Var;
import exm.stc.common.lang.Var.DefType;
import exm.stc.common.lang.Var.VarStorage;
import exm.stc.frontend.Context.DefKind;

/**
 * Track context within a function.  New child contexts are created
 * for every new variable scope.
 *
 */
public class LocalContext extends Context {
  private final Context parent;
  private final GlobalContext globals;
  private final FunctionContext functionContext;

  public LocalContext(Context parent) {
    this(parent, null);
  }

  public LocalContext(Context parent, String functionName) {
    super(parent.getLogger(), parent.getLevel() + 1);
    this.functionContext = functionName != null ?
          new FunctionContext(functionName) : null;
    this.parent = parent;
    this.globals = parent.getGlobals();
    inputFile = parent.inputFile;
    line = parent.line;
    col = parent.col;
  }
  
  @Override
  public DefInfo lookupDef(String name) {
    DefInfo result = allDefs.get(name);
    if (result != null) {
      return result;
    } else {
      return parent.lookupDef(name);
    }
  }

  @Override
  public Var lookupVarUnsafe(String name) {
    Var result;
    result = variables.get(name);
    if (result != null)
      return result;
    return parent.lookupVarUnsafe(name);
  }

  @Override
  public Var createTmpVar(Type type, boolean storeInStack) 
                                                      throws UserException {
      String name;
      do {
        int counter = getFunctionContext().getCounterVal("intermediate_var");
        name = Var.TMP_VAR_PREFIX + counter;
      } while (lookupDef(name) != null); // In case variable name in use

      VarStorage storage = storeInStack ? 
                  VarStorage.STACK : VarStorage.TEMP;
      return declareVariable(type, name, storage, DefType.LOCAL_COMPILER, null);
  }

  @Override
  public Var createAliasVariable(Type type) throws UserException {
    String name;
    do {
      int counter = getFunctionContext().getCounterVal("alias_var");
      name = Var.ALIAS_VAR_PREFIX + counter;
    } while (lookupDef(name) != null);

    return declareVariable(type, name, VarStorage.ALIAS,
                           DefType.LOCAL_COMPILER, null);
  }

  /**
   *
   * @param type
   * @param varName the name of the variable this is the value of:
   *    try and work this into the generated name. Can be left as
   *    null
   * @return
   * @throws UserException
   */
  @Override
  public Var createLocalValueVariable(Type type, String varName)
      throws UserException {
    String name = chooseVariableName(Var.LOCAL_VALUE_VAR_PREFIX, varName,
                                    "value_var");
    return declareVariable(type, name, VarStorage.LOCAL,
                           DefType.LOCAL_COMPILER, null);
  }

  /**
   * Helper to choose variable name.  
   * @param prefix Prefix that must be at start
   * @param preferredSuffix Preferred suffix
   * @param counterName name of counter to use to make unique if needed
   * @return
   */
  private String chooseVariableName(String prefix, String preferredSuffix,
      String counterName) {
    if (preferredSuffix != null) {
      prefix += preferredSuffix;
      // see if we can give it a nice name
      if (lookupDef(prefix) == null) {
        return prefix;
      }
    }

    String name = null;
    do {
      int counter = getFunctionContext().getCounterVal(counterName);
      name = prefix + counter;
    } while (lookupDef(name) != null);
    return name;
  }

  @Override
  public Var createFilenameAliasVariable(String fileVarName) {
    String name = chooseVariableName(Var.FILENAME_OF_PREFIX,
        fileVarName, "filename_of");
    try {
      return declareVariable(Types.F_STRING, name,
          VarStorage.ALIAS, DefType.LOCAL_COMPILER, null);
    } catch (DoubleDefineException e) {
      e.printStackTrace();
      throw new STCRuntimeError("Should be possible to have double defn");
    }
  }

  @Override
  public void defineFunction(String name, FunctionType type)
                                    throws DoubleDefineException {
    throw new STCRuntimeError("Cannot define function in local context");
  }

  @Override
  public void setFunctionProperty(String name, FnProp prop) {
    throw new STCRuntimeError("Cannot define function in local context");
  }

  @Override
  public boolean hasFunctionProp(String name, FnProp prop) {
    return parent.hasFunctionProp(name, prop);
  }
  
  @Override
  public List<FnProp> getFunctionProps(String function) {
    return parent.getFunctionProps(function);
  }

  @Override
  public GlobalContext getGlobals() {
    return globals;
  }

  @Override
  public List<Var> getVisibleVariables() {
    List<Var> result = new ArrayList<Var>();

    // All variable from parent visible, plus variables defined in this scope
    result.addAll(parent.getVisibleVariables());
    result.addAll(variables.values());

    return result;
  }

  public void addDeclaredVariables(List<Var> variables)
        throws DoubleDefineException {
    for (Var v : variables)
      declareVariable(v);
  }

  @Override
  public String toString() {
    return getVisibleVariables().toString();
  }

  @Override
  public Type lookupTypeUnsafe(String typeName) {
    Type t = types.get(typeName);
    if (t != null) {
      return t;
    } else {
      return parent.lookupTypeUnsafe(typeName);
    }
  }

  /**
   * Called when we want to create a new alias for a structure filed
   */
  @Override
  protected Var createStructFieldTmp(Var struct,
      Type fieldType, String fieldPath, VarStorage storage) {
    // Should be unique in context
    String basename = Var.STRUCT_FIELD_VAR_PREFIX
        + struct.name() + "_" + fieldPath.replace('.', '_');
    String name = basename;
    int counter = 1;
    while (lookupDef(name) != null) {
      name = basename + "-" + counter;
      counter++;
    }
    try {
      return declareVariable(fieldType, name, storage, DefType.LOCAL_COMPILER,
                           null);
    } catch (DoubleDefineException e) {
      e.printStackTrace();
      throw new STCRuntimeError("Shouldn't be possible to have double defn");
    }
  }
  
  @Override
  public FunctionContext getFunctionContext() {
    if (this.functionContext != null) {
      return this.functionContext;
    } else {
      return parent.getFunctionContext();
    }
  }
}
