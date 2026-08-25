package com.momentum.nutrition.dto;

public interface BaseFoodData {

    String getName();
    void setName(String name);

    String getImagePath();
    void setImagePath(String imagePath);

    Integer getCalories();
    void setCalories(Integer calories);

    Double getCarbohydrates();
    void setCarbohydrates(Double carbohydrates);

    Double getSugar();
    void setSugar(Double sugar);

    Double getFiber();
    void setFiber(Double fiber);

    Double getGlycemicIndex();
    void setGlycemicIndex(Double glycemicIndex);

    Double getGlycemicLoad();
    void setGlycemicLoad(Double glycemicLoad);

    Double getProtein();
    void setProtein(Double protein);

    Double getFat();
    void setFat(Double fat);

    Double getSaturatedFat();
    void setSaturatedFat(Double saturatedFat);

    Double getMonoUnsaturated();
    void setMonoUnsaturated(Double monoUnsaturated);

    Double getPolyUnsaturated();
    void setPolyUnsaturated(Double polyUnsaturated);

    Double getTransFat();
    void setTransFat(Double transFat);

    Double getCholesterol();
    void setCholesterol(Double cholesterol);

    Double getCaffeine();
    void setCaffeine(Double caffeine);

    Double getAlcohol();
    void setAlcohol(Double alcohol);

    Double getSodium();
    void setSodium(Double sodium);

    Double getPotassium();
    void setPotassium(Double potassium);

    Double getCalcium();
    void setCalcium(Double calcium);

    Double getIron();
    void setIron(Double iron);

    Integer getServingSize();
    void setServingSize(Integer servingSize);
}



