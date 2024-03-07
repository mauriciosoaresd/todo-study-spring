import { useEffect, useState } from "react";
import "./App.css";
import Card from "./components/Card";
import AddTodoModal from "./components/AddTodoModal";

function App() {
  const [todos, setTodos] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);

  const updateTodo = (todo) => {
    let todoIndex = todos.findIndex(prevTodo => prevTodo.id == todo.id);
    let newTodoList = [...todos];
    newTodoList[todoIndex] = todo;
    setTodos(newTodoList);
  }

  const getTodos = async (pageIndex) => {
    const fetchedTodos = await fetch(`/api/todo?page=${pageIndex || 0}&limit=20&sortfield=PRIORITY&sortorder=DESC`)
    .then(async (res) => {
      const response = res.json();
      if(res.ok) {
        return response;
      } else {
        return []
      }
    });
    setTodos((prevState) => {
      if(prevState !== null) {
        return prevState.concat(fetchedTodos.content)
      } else {
        return [].concat(fetchedTodos.content)
      }
    });
    setPageNumber(pageIndex || 0);

    if (fetchedTodos.last) {
      setPageNumber(null);
    }
  };

  useEffect(() => {
    getTodos();
  }, []);

  return (
    <>
      <h1>To do List</h1>
      <h1>{pageNumber}</h1>
      <div className="d-flex flex-wrap gap-3 mb-3">
        {todos == null ? (
          <h1>Loading...</h1>
        ) : todos.length == 0 ? (
          <h3>Empty to do list</h3>
        ) : (
          todos.map((todo, id) => (
            <Card key={id} todo={todo} updateTodo={updateTodo} setTodos={setTodos}/>
          ))
        )}
      </div>
      <AddTodoModal getTodos={getTodos} setTodos={setTodos} />
      {pageNumber != null && (
        <button className="rounded" onClick={() => getTodos(pageNumber + 1)}>
          Load more
        </button>
      )}
    </>
  );
}

export default App;
