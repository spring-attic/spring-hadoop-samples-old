package com.oreilly.springdata.batch.item;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.data.hadoop.example.domain.Product;

public class ProductProcessor implements ItemProcessor<Product, Product> {

	@Override
	public Product process(Product product) throws Exception {		
		if (product.getId().startsWith("PR1")) {
			return null;
		} else {
			return product;
		}
	}

}
