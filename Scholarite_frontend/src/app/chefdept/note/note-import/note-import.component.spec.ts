import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoteImportComponent } from './note-import.component';

describe('NoteImportComponent', () => {
  let component: NoteImportComponent;
  let fixture: ComponentFixture<NoteImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteImportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NoteImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
