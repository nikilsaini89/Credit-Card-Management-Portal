export interface CardType {
  name: string;
  networkType: string;
  description: string;
}

export interface CreditCard {
  id: number;
  cardHolderName: string;
  cardNumber: string;
  cardStatus: string;
  creditLimit: number;
  availableLimit: number;
  expiryDate: string;   
  cardType: CardType;
}
