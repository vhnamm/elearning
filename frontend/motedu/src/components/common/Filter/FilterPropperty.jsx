import React from 'react';
import { useState } from 'react';
import styles from './FilterProperty.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMinus, faPlus } from '@fortawesome/free-solid-svg-icons'; 

const FilterProperty = ({type, children}) => {
  const [openOption, setOpenOption] = useState(false);

  return (
   <div className={styles.filter_property}>
    <div onClick={() => setOpenOption(!openOption)} className={styles.property_header}>
      <h4>{type}</h4>
      {!openOption && <FontAwesomeIcon icon={faPlus} className={styles.icon_more}/>}
      {openOption && <FontAwesomeIcon icon={faMinus} className={styles.icon_less}/>}
    </div>
    {openOption && children}
   </div>
  );
};

export default FilterProperty;