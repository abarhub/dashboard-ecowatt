import {Statut} from "./statut";
import {Heure} from "./heure";

export class Journee {

  date: Date | null = null;
  message: string = '';

  statut: Statut = Statut.UNKNOW;

  heures: Heure[] = [];
}
