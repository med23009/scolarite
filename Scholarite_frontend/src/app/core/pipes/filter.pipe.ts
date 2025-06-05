import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter',
  standalone: true
})
export class FilterPipe implements PipeTransform {
  transform(items: any[], id: string | null, idField: string): any {
    if (!items || !id) {
      return null;
    }
    
    return items.find(item => item[idField]?.toString() === id.toString());
  }
}
