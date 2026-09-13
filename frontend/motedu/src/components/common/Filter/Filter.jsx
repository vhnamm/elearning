import { faXmark } from '@fortawesome/free-solid-svg-icons';
import React, { useState } from 'react';
import styles from './Filter.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import clsx from 'clsx';
import FilterProperty from './FilterPropperty';
import Button from '../Button/Button';

const colors = ['Black', 'Cream', 'Red', 'Gray', 'Brown', 'White'];
const priceRanges = ['Under 200.000đ', '200.000đ - 499.000đ', '500.000đ - 999.000đ', 'Above 1.000.000đ'];
const sizes = ["S", "M", "L", "XL", "XXL"];

const Filter = ({onClose, gender=true}) => {
  const [activeColors, setActiveColors] = useState([]);
  const [activeSizes, setActiveSizes] = useState([]);
  

  function handleActiveColor(e, index){
    let i = activeColors.indexOf(index);
    if(i == -1){
      activeColors.push(index);
    }else{
      activeColors.splice(i, 1);
    }
    
    setActiveColors([...activeColors])
  }

  function handleActiveSize(e, index){
    let i = activeSizes.indexOf(index);   
    if(i == -1){
      activeSizes.push(index);
    }else{
      activeSizes.splice(i, 1);
    } 
    setActiveSizes([...activeSizes])
  }

  return (
    <div className={styles.filter_modal_wrap}>
      <div className={styles.filter_header}>
        <h4>Filter</h4>       
        <div onClick={() => onClose()}>
          <FontAwesomeIcon icon={faXmark} className={styles.icon}/>
        </div>
        
      </div>

      <div>

        <FilterProperty type="Price">
            <div className={clsx("grid")}>
                <div className={clsx("row")}>
                    {priceRanges.map((range, index) => {
                      return(
                        <div  
                          className={clsx("col", "lg-6", styles.option)}
                          key={index}>
                          <input id={index} type="checkbox" />
                          <label htmlFor={index}>{range}</label>
                        </div>
                      )
                    })}
                </div>              
            </div>            
        </FilterProperty>

        <FilterProperty type="Color">
          <div className={clsx("grid")}>
            <div className={clsx("row", "small-gutter")}>

                {colors.map((color, index) => {
                  return (
                    <div className={clsx("col", "lg-3", styles.color_option_wrap)}>
                      <div onClick={(e) => handleActiveColor(e, index)} 
                      className={clsx(styles.color_option, {[styles.active] : activeColors.includes(index)})}
                      
                      >
                      <div className={clsx(styles.color_round, styles[color])}></div>
                      <span>{color}</span>
                    </div>
                </div>
                  )
                })}
            </div>           
        </div>
        </FilterProperty>

        <FilterProperty type="Size">
          <div className={clsx("grid")}>
            <div className={clsx("row", "small-gutter")}>

                {sizes.map((size, index) => {
                  return (
                    <div className={clsx("col", "lg-2-5", styles.size_option_wrap)}>
                      <div 
                        className={clsx(styles.size_option, {[styles.active] : activeSizes.includes(index)})}
                        onClick={(e) => handleActiveSize(e, index)}
                      >
                      <h4>{size}</h4>
                    </div>
                </div>
                  )
                })}
            </div>           
        </div>
        </FilterProperty>

        {gender && <FilterProperty type="Gender">
          <div className={clsx("grid")}>
            <div className={clsx("row", "small-gutter")}>
                {
            
                  ["Male", "Female", "Unisex"].map((gender, index) => {
                    return(
                      <div  
                          className={clsx("col", "lg-6", styles.option)}
                          key={gender}>
                          <input id={gender} type="checkbox" />
                          <label htmlFor={gender}>{gender}</label>
                      </div>
                      )
                    })
                }
            </div>
          </div>
          
        </FilterProperty>}
      </div>

      <div className={styles.filter_modal_bottom}>
        <Button className={styles.btn_reset} size="medium">Reset</Button>
        <Button className={styles.btn_apply} size="medium">Apply filter</Button>
      </div>
    </div>
  );
};

export default Filter;  