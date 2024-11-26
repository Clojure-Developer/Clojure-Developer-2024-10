(ns otus-06.queries)

(def select-customers-query
  {
   :select [:customer-id :name :address :phone-number]
   :from   :customers
   })

(def select-products-query
  {
   :select [:product-id :description :cost]
   :from   :products
   })


(def select-sales-query
  {
   :select [:name :description :item-count]
   :from   :sales
   :join   [{:table :customers
             :on    [:customer-id :customer-id]
             :type  :left}
            {:table :products
             :on    [:product-id :product-id]
             :type  :left}]
   })


(defn select-total-for-customer-query [name]
  {
   :select          [:name :total-cost]
   :from            :sales
   :join            [{:table :customers
                      :on    [:customer-id :customer-id]
                      :type  :left}
                     {:table :products
                      :on    [:product-id :product-id]
                      :type  :left}]
   :group-by        [:name]
   :group-aggregate [[:product [:item-count :cost] :total-cost]]
   :where #(= (:name %) name)
   })

(defn select-total-count-for-product [product]
  {
   :select          [:description :total-count]
   :from            :sales
   :join            [{:table :products
                      :on    [:product-id :product-id]
                      :type  :left}]
   :group-by        [:description]
   :group-aggregate [[:sum [:item-count] :total-count]]
   :where #(= (:description %) product)
   })
