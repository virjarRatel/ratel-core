package com.virjar.ratel.api.extension.superappium.xpath.function;

import android.util.Log;

import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.AncestorFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.AncestorOrSelfFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.AxisFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.ChildFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.DescendantFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.DescendantOrSelfFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.FollowingSiblingFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.FollowingSiblingOneFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.ParentFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.PrecedingSiblingFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.PrecedingSiblingOneFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.axis.SiblingFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.AbsFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.BooleanFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.ConcatFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.ContainsFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.FalseFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.FilterFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.FirstFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.LastFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.LowerCaseFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.MatchesFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.NameFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.NotFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.NullToDefaultFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.PositionFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.PredicateFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.RootFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.SipSoupPredicteJudgeFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.StringFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.StringLengthFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.SubstringFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.ToDoubleFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.ToIntFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.TrueFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.TryExeptionFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.filter.UpperCaseFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.select.AttrFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.select.SelectFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.select.SelfFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.select.TagSelectFunction;
import com.virjar.ratel.api.extension.superappium.xpath.function.select.TextFunction;

import java.util.HashMap;
import java.util.Map;

public class FunctionEnv {
    private static Map<String, SelectFunction> selectFunctions = new HashMap<>();
    private static Map<String, FilterFunction> filterFunctions = new HashMap<>();
    private static Map<String, AxisFunction> axisFunctions = new HashMap<>();

    static {
        registerAllSelectFunctions();
        registerAllFilterFunctions();
        registerAllAxisFunctions();

    }

    public static SelectFunction getSelectFunction(String functionName) {
        return selectFunctions.get(functionName);
    }

    public static FilterFunction getFilterFunction(String functionName) {
        return filterFunctions.get(functionName);
    }

    public static AxisFunction getAxisFunction(String functionName) {
        return axisFunctions.get(functionName);
    }

    public synchronized static void registerSelectFunction(SelectFunction selectFunction) {
        if (selectFunctions.containsKey(selectFunction.getName())) {
            Log.w(SuperAppium.TAG, "register a duplicate  select function " + selectFunction.getName());
        }
        selectFunctions.put(selectFunction.getName(), selectFunction);
    }

    public synchronized static void registerFilterFunction(FilterFunction filterFunction) {
        if (filterFunctions.containsKey(filterFunction.getName())) {
            Log.w(SuperAppium.TAG, "register a duplicate  filter function " + filterFunction.getName());
        }
        filterFunctions.put(filterFunction.getName(), filterFunction);
    }

    public synchronized static void registerAxisFunciton(AxisFunction axisFunction) {
        if (axisFunctions.containsKey(axisFunction.getName())) {
            Log.w(SuperAppium.TAG, "register a duplicate  axis function " + axisFunction.getName());
        }
        axisFunctions.put(axisFunction.getName(), axisFunction);
    }

    private static void registerAllSelectFunctions() {
        registerSelectFunction(new AttrFunction());
        registerSelectFunction(new SelfFunction());
        registerSelectFunction(new TagSelectFunction());
        registerSelectFunction(new TextFunction());
    }

    private static void registerAllFilterFunctions() {
        registerFilterFunction(new AbsFunction());
        registerFilterFunction(new BooleanFunction());
        registerFilterFunction(new ConcatFunction());
        registerFilterFunction(new ContainsFunction());
        registerFilterFunction(new FalseFunction());
        registerFilterFunction(new FirstFunction());
        registerFilterFunction(new LastFunction());
        registerFilterFunction(new LowerCaseFunction());
        registerFilterFunction(new MatchesFunction());
        registerFilterFunction(new NameFunction());
        registerFilterFunction(new NotFunction());
        registerFilterFunction(new NullToDefaultFunction());
        registerFilterFunction(new PositionFunction());
        registerFilterFunction(new PredicateFunction());
        registerFilterFunction(new RootFunction());
        registerFilterFunction(new StringFunction());
        registerFilterFunction(new StringLengthFunction());
        registerFilterFunction(new SubstringFunction());
        registerFilterFunction(new com.virjar.ratel.api.extension.superappium.xpath.function.filter.TextFunction());
        registerFilterFunction(new ToDoubleFunction());
        registerFilterFunction(new ToIntFunction());
        registerFilterFunction(new TrueFunction());
        registerFilterFunction(new TryExeptionFunction());
        registerFilterFunction(new UpperCaseFunction());
        registerFilterFunction(new SipSoupPredicteJudgeFunction());
    }

    private static void registerAllAxisFunctions() {
        registerAxisFunciton(new AncestorFunction());
        registerAxisFunciton(new AncestorOrSelfFunction());
        registerAxisFunciton(new ChildFunction());
        registerAxisFunciton(new DescendantFunction());
        registerAxisFunciton(new DescendantOrSelfFunction());
        registerAxisFunciton(new FollowingSiblingFunction());
        registerAxisFunciton(new FollowingSiblingOneFunction());
        registerAxisFunciton(new PrecedingSiblingOneFunction());
        registerAxisFunciton(new ParentFunction());
        registerAxisFunciton(new PrecedingSiblingFunction());
        registerAxisFunciton(new com.virjar.ratel.api.extension.superappium.xpath.function.axis.SelfFunction());
        registerAxisFunciton(new SiblingFunction());
    }


}
